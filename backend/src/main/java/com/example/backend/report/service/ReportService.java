package com.example.backend.report.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.report.dto.ReportItemDto;
import com.example.backend.report.dto.ReportPageResponse;
import com.example.backend.report.dto.ReportSummaryResponse;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service aggregating candidate assessment evaluation reports, metrics, and KPI analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;

    /**
     * Retrieves paginated assessment reports, optionally filtered by workspace and status.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId Optional workspace filter.
     * @param status      Optional status filter.
     * @param page        Page index (0-based).
     * @param size        Page size.
     * @return ReportPageResponse with paginated ReportItemDto records.
     */
    @Transactional(readOnly = true)
    public ReportPageResponse getWorkspaceReports(UUID recruiterId, UUID workspaceId, AssessmentStatus status, int page, int size) {
        log.info("Fetching reports for workspace={}, recruiter={}, status={}, page={}, size={}",
                workspaceId, recruiterId, status, page, size);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Assessment> assessmentPage;

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

            if (recruiterId != null && workspace.getRecruiter() != null && !workspace.getRecruiter().getId().equals(recruiterId)) {
                throw new ForbiddenException("Recruiter does not own this workspace");
            }

            if (status != null) {
                assessmentPage = assessmentRepository.findAllByWorkspaceIdAndStatus(workspaceId, status, pageable);
            } else {
                assessmentPage = assessmentRepository.findAllByWorkspaceId(workspaceId, pageable);
            }
        } else {
            // Fetch all assessments for all recruiter workspaces
            if (recruiterId != null) {
                assessmentPage = assessmentRepository.findAllByRecruiterId(recruiterId, pageable);
            } else {
                assessmentPage = assessmentRepository.findAll(pageable);
            }
        }

        List<ReportItemDto> items = assessmentPage.getContent().stream().map(a -> {
            var submissionOpt = submissionRepository.findByAssessmentId(a.getId());
            Instant submittedAt = submissionOpt.map(s -> s.getSubmittedAt()).orElse(a.getUpdatedAt());
            BigDecimal score = submissionOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()))
                    .map(r -> r.getScore())
                    .orElse(BigDecimal.ZERO);

            return ReportItemDto.builder()
                    .assessmentId(a.getId())
                    .candidateId(a.getCandidate() != null ? a.getCandidate().getId() : null)
                    .candidateName(a.getCandidate() != null ? a.getCandidate().getName() : "Unknown")
                    .candidateEmail(a.getCandidate() != null ? a.getCandidate().getEmail() : "Unknown")
                    .workspaceName(a.getWorkspace() != null ? a.getWorkspace().getName() : "General")
                    .score(score)
                    .status(a.getStatus())
                    .submittedAt(submittedAt)
                    .build();
        }).toList();

        return ReportPageResponse.builder()
                .content(items)
                .page(assessmentPage.getNumber())
                .size(assessmentPage.getSize())
                .totalElements(assessmentPage.getTotalElements())
                .totalPages(assessmentPage.getTotalPages())
                .build();
    }

    /**
     * Calculates live KPI analytics and summary metrics across workspaces.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId Optional workspace filter.
     * @return ReportSummaryResponse containing calculated rates and averages.
     */
    @Transactional(readOnly = true)
    public ReportSummaryResponse getReportSummary(UUID recruiterId, UUID workspaceId) {
        log.info("Calculating report summary for recruiter={}, workspaceId={}", recruiterId, workspaceId);

        List<Assessment> assessments;
        long totalCandidates;

        if (workspaceId != null) {
            assessments = assessmentRepository.findAllByWorkspaceId(workspaceId);
            totalCandidates = workspaceCandidateRepository.findAllByWorkspaceId(workspaceId).size();
        } else if (recruiterId != null) {
            var workspaces = workspaceRepository.findAllByRecruiterId(recruiterId);
            List<UUID> wsIds = workspaces.stream().map(Workspace::getId).toList();
            assessments = new ArrayList<>();
            for (UUID wsId : wsIds) {
                assessments.addAll(assessmentRepository.findAllByWorkspaceId(wsId));
            }
            totalCandidates = workspaceCandidateRepository.countDistinctCandidatesByRecruiterId(recruiterId);
        } else {
            assessments = assessmentRepository.findAll();
            totalCandidates = workspaceCandidateRepository.count();
        }

        long completedCount = assessments.stream().filter(a -> a.getStatus() == AssessmentStatus.COMPLETED).count();
        long scheduledCount = assessments.stream().filter(a -> a.getStatus() == AssessmentStatus.SCHEDULED || a.getStatus() == AssessmentStatus.READY).count();

        int participationRate = totalCandidates > 0
                ? (int) Math.round(((double) (completedCount + scheduledCount) / totalCandidates) * 100)
                : 0;

        List<BigDecimal> scores = new ArrayList<>();
        long passedCount = 0;

        for (Assessment a : assessments) {
            if (a.getStatus() == AssessmentStatus.COMPLETED) {
                var scoreOpt = submissionRepository.findByAssessmentId(a.getId())
                        .flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()))
                        .map(r -> r.getScore());
                if (scoreOpt.isPresent()) {
                    BigDecimal score = scoreOpt.get();
                    scores.add(score);
                    if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
                        passedCount++;
                    }
                }
            }
        }

        int passRate = completedCount > 0
                ? (int) Math.round(((double) passedCount / completedCount) * 100)
                : 0;

        BigDecimal avgScore = BigDecimal.ZERO;
        if (!scores.isEmpty()) {
            BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            avgScore = sum.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
        }

        return ReportSummaryResponse.builder()
                .totalCandidates(totalCandidates)
                .completedAssessments(completedCount)
                .scheduledAssessments(scheduledCount)
                .participationRate(participationRate)
                .passedAssessments(passedCount)
                .passRate(passRate)
                .averageScore(avgScore)
                .build();
    }
}
