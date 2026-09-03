package com.example.backend.assessment.application;

import com.example.backend.assessment.dto.AssessmentResponse;
import com.example.backend.assessment.dto.CreateAssessmentRequest;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.event.AssessmentCreatedEvent;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.ValidationException;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.entity.WorkspaceCandidateId;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentCommandService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final ApplicationEventPublisher eventPublisher;

    public AssessmentResponse createAssessment(UUID recruiterId, UUID workspaceId, CreateAssessmentRequest request) {
        log.info("Creating assessment in workspace: {} for candidate: {}", workspaceId, request.getCandidateId());
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + request.getCandidateId()));

        if (!workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            WorkspaceCandidate wc = WorkspaceCandidate.builder()
                    .workspace(workspace)
                    .candidate(candidate)
                    .id(new WorkspaceCandidateId(workspace.getId(), candidate.getId()))
                    .build();
            workspaceCandidateRepository.save(wc);
        }

        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new ValidationException("Duration must be greater than 0", "VALIDATION_ERROR");
        }

        if (request.getScheduledEndAt().isBefore(request.getScheduledStartAt()) ||
            request.getScheduledEndAt().equals(request.getScheduledStartAt())) {
            throw new ValidationException("Scheduled end time must be after scheduled start time", "VALIDATION_ERROR");
        }

        Assessment assessment = Assessment.builder()
                .workspace(workspace)
                .candidate(candidate)
                .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle().trim() : "Java Spring Boot Technical Assessment")
                .repositoryUrl(request.getRepositoryUrl().trim())
                .branchName(request.getBranchName().trim())
                .backendRootDirectory(request.getBackendRootDirectory() != null ? request.getBackendRootDirectory().trim() : "")
                .difficulty(request.getDifficulty())
                .durationMinutes(request.getDurationMinutes())
                .scheduledStartAt(request.getScheduledStartAt())
                .scheduledEndAt(request.getScheduledEndAt())
                .status(AssessmentStatus.CREATING)
                .build();

        Assessment saved = assessmentRepository.save(assessment);
        log.info("Saved assessment ID: {}", saved.getId());

        // Publish event to launch the background AI processing pipeline post-transaction commit
        eventPublisher.publishEvent(new AssessmentCreatedEvent(
                this,
                saved.getId(),
                saved.getRepositoryUrl(),
                saved.getBranchName(),
                saved.getBackendRootDirectory()
        ));

        return mapToAssessmentResponse(saved);
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .assessmentId(assessment.getId())
                .workspaceId(assessment.getWorkspace().getId())
                .candidateId(assessment.getCandidate().getId())
                .title(assessment.getTitle() != null ? assessment.getTitle() : "Java Spring Boot Technical Assessment")
                .repositoryUrl(assessment.getRepositoryUrl())
                .branchName(assessment.getBranchName())
                .backendRootDirectory(assessment.getBackendRootDirectory())
                .difficulty(assessment.getDifficulty())
                .durationMinutes(assessment.getDurationMinutes())
                .scheduledStartAt(assessment.getScheduledStartAt())
                .scheduledEndAt(assessment.getScheduledEndAt())
                .status(assessment.getStatus())
                .build();
    }
}
