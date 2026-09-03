package com.example.backend.dashboard.service;

import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import com.example.backend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruiterDashboardService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final WorkspaceService workspaceService;

    public RecruiterDashboardResponse getRecruiterDashboard(UUID recruiterId) {
        log.info("Calculating recruiter dashboard metrics for recruiterId: {}", recruiterId);
        User recruiter = workspaceService.getRecruiterOrThrow(recruiterId);

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
}
