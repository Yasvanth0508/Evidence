package com.example.backend.dashboard.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.EvaluationReport;
import com.example.backend.assessment.entity.Submission;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.dto.CompletedAssessmentDto;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.dashboard.dto.ScheduledAssessmentDto;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import com.example.backend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;

    public RecruiterDashboardResponse getRecruiterDashboard(UUID recruiterId) {
        log.info("Calculating recruiter dashboard metrics for recruiterId: {}", recruiterId);
        User recruiter = workspaceService.getOrCreateRecruiter(recruiterId);

        long workspaceCount = workspaceRepository.countByRecruiterId(recruiter.getId());
        long candidateCount = workspaceCandidateRepository.countDistinctCandidatesByRecruiterId(recruiter.getId());
        long assessmentCount = assessmentRepository.countByRecruiterId(recruiter.getId());

        List<AssessmentStatus> activeStatuses = List.of(
                AssessmentStatus.CREATING,
                AssessmentStatus.ANALYZING,
                AssessmentStatus.GENERATING_FEATURE,
                AssessmentStatus.GENERATING_TESTS,
                AssessmentStatus.READY,
                AssessmentStatus.SCHEDULED,
                AssessmentStatus.IN_PROGRESS,
                AssessmentStatus.EVALUATING
        );
        long activeAssessments = assessmentRepository.countByRecruiterIdAndStatusIn(recruiter.getId(), activeStatuses);
        long completedAssessments = assessmentRepository.countByRecruiterIdAndStatusIn(recruiter.getId(), List.of(AssessmentStatus.COMPLETED));

        return RecruiterDashboardResponse.builder()
                .workspaceCount(workspaceCount)
                .candidateCount(candidateCount)
                .assessmentCount(assessmentCount)
                .activeAssessments(activeAssessments)
                .completedAssessments(completedAssessments)
                .build();
    }

    public CandidateDashboardResponse getCandidateDashboard(UUID candidateId) {
        log.info("Calculating candidate dashboard for candidateId: {}", candidateId);
        User candidate = getCandidate(candidateId);

        List<Assessment> assessments = assessmentRepository.findAllByCandidateIdOrderByCreatedAtDesc(candidate.getId());

        List<ScheduledAssessmentDto> scheduledList = new ArrayList<>();
        List<CompletedAssessmentDto> completedList = new ArrayList<>();

        for (Assessment a : assessments) {
            if (a.getStatus() == AssessmentStatus.COMPLETED) {
                Optional<Submission> subOpt = submissionRepository.findByAssessmentId(a.getId());
                Long timeTaken = subOpt.map(Submission::getTimeTakenSeconds).orElse(0L);
                var submittedAt = subOpt.map(Submission::getSubmittedAt).orElse(a.getScheduledEndAt());

                BigDecimal score = BigDecimal.ZERO;
                if (subOpt.isPresent()) {
                    Optional<EvaluationReport> evalOpt = evaluationReportRepository.findBySubmissionId(subOpt.get().getAssessmentId());
                    score = evalOpt.map(EvaluationReport::getScore).orElse(BigDecimal.ZERO);
                }

                completedList.add(CompletedAssessmentDto.builder()
                        .assessmentId(a.getId())
                        .workspaceId(a.getWorkspace().getId())
                        .workspaceName(a.getWorkspace().getName())
                        .submittedAt(submittedAt)
                        .timeTakenSeconds(timeTaken)
                        .score(score)
                        .status(a.getStatus())
                        .build());
            } else {
                scheduledList.add(ScheduledAssessmentDto.builder()
                        .assessmentId(a.getId())
                        .workspaceId(a.getWorkspace().getId())
                        .workspaceName(a.getWorkspace().getName())
                        .scheduledStartAt(a.getScheduledStartAt())
                        .scheduledEndAt(a.getScheduledEndAt())
                        .difficulty(a.getDifficulty())
                        .status(a.getStatus())
                        .build());
            }
        }

        return CandidateDashboardResponse.builder()
                .scheduledAssessments(scheduledList)
                .completedAssessments(completedList)
                .build();
    }

    private User getCandidate(UUID candidateId) {
        if (candidateId != null) {
            return userRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId, "CANDIDATE_NOT_FOUND"));
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CANDIDATE)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found", "CANDIDATE_NOT_FOUND"));
    }
}
