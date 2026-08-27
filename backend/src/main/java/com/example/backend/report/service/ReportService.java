package com.example.backend.report.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.report.dto.ReportItemDto;
import com.example.backend.report.dto.ReportPageResponse;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SubmissionRepository submissionRepository;
    private final com.example.backend.assessment.repository.EvaluationReportRepository evaluationReportRepository;

    /**
     * Phase D.1: List Workspace Assessment Reports with Filters and Pagination.
     */
    @Transactional(readOnly = true)
    public ReportPageResponse getWorkspaceReports(UUID recruiterId, UUID workspaceId, AssessmentStatus status, int page, int size) {
        log.info("Fetching reports for workspace {} by recruiter {}, status={}, page={}, size={}",
                workspaceId, recruiterId, status, page, size);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        if (recruiterId != null && workspace.getRecruiter() != null && !workspace.getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter does not own this workspace");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Assessment> assessmentPage;

        if (status != null) {
            assessmentPage = assessmentRepository.findAllByWorkspaceIdAndStatus(workspaceId, status, pageable);
        } else {
            assessmentPage = assessmentRepository.findAllByWorkspaceId(workspaceId, pageable);
        }

        List<ReportItemDto> items = assessmentPage.getContent().stream().map(a -> {
            var submissionOpt = submissionRepository.findByAssessmentId(a.getId());
            Instant submittedAt = submissionOpt.map(s -> s.getSubmittedAt()).orElse(a.getUpdatedAt());
            java.math.BigDecimal score = submissionOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()))
                    .map(r -> r.getScore())
                    .orElse(java.math.BigDecimal.ZERO);

            return ReportItemDto.builder()
                    .assessmentId(a.getId())
                    .candidateId(a.getCandidate() != null ? a.getCandidate().getId() : null)
                    .candidateName(a.getCandidate() != null ? a.getCandidate().getName() : "Unknown")
                    .candidateEmail(a.getCandidate() != null ? a.getCandidate().getEmail() : "Unknown")
                    .workspaceName(workspace.getName())
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
}
