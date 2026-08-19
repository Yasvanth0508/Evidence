package com.example.backend.dashboard.service;

import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Role;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecruiterDashboardService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;

    public RecruiterDashboardService(WorkspaceRepository workspaceRepository,
                                     WorkspaceCandidateRepository workspaceCandidateRepository,
                                     AssessmentRepository assessmentRepository,
                                     UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceCandidateRepository = workspaceCandidateRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
    }

    public RecruiterDashboardResponse getDashboard() {
        // Resolve default recruiter (recruiter@example.com) for pre-auth development
        UUID recruiterId = userRepository.findByEmail("recruiter@example.com")
                .map(User::getId)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.RECRUITER)
                        .findFirst()
                        .map(User::getId)
                        .orElse(UUID.randomUUID()));

        long workspaceCount = workspaceRepository.countByRecruiterId(recruiterId);
        long candidateCount = workspaceCandidateRepository.countDistinctCandidatesByRecruiterId(recruiterId);
        long assessmentCount = assessmentRepository.countByRecruiterId(recruiterId);

        List<AssessmentStatus> activeStatuses = List.of(
                AssessmentStatus.SCHEDULED,
                AssessmentStatus.READY,
                AssessmentStatus.CREATING,
                AssessmentStatus.IN_PROGRESS
        );
        long activeAssessments = assessmentRepository.countByRecruiterIdAndStatusIn(recruiterId, activeStatuses);

        List<AssessmentStatus> completedStatuses = List.of(
                AssessmentStatus.COMPLETED,
                AssessmentStatus.EVALUATING
        );
        long completedAssessments = assessmentRepository.countByRecruiterIdAndStatusIn(recruiterId, completedStatuses);

        return new RecruiterDashboardResponse(
                workspaceCount,
                candidateCount,
                assessmentCount,
                activeAssessments,
                completedAssessments
        );
    }
}
