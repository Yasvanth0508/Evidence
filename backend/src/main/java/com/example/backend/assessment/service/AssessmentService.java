package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.*;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.*;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceService workspaceService;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             WorkspaceService workspaceService,
                             WorkspaceCandidateRepository workspaceCandidateRepository,
                             UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.workspaceService = workspaceService;
        this.workspaceCandidateRepository = workspaceCandidateRepository;
        this.userRepository = userRepository;
    }

    public AssessmentResponse createAssessment(UUID recruiterId, UUID workspaceId, CreateAssessmentRequest request) {
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        User candidate = userRepository.findById(request.getCandidateId())
                .filter(u -> u.getRole() == Role.CANDIDATE)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found", "CANDIDATE_NOT_FOUND"));

        if (!workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            throw new ValidationException(
                    "Candidate is not enrolled in this workspace. Please add the candidate to the workspace first.",
                    "CANDIDATE_NOT_IN_WORKSPACE"
            );
        }

        if (!request.getScheduledEndAt().isAfter(request.getScheduledStartAt())) {
            throw new ValidationException("Scheduled end time must be after scheduled start time.", "VALIDATION_ERROR");
        }

        Assessment assessment = new Assessment(
                workspace,
                candidate,
                request.getRepositoryUrl().trim(),
                request.getBranchName().trim(),
                request.getBackendRootDirectory().trim(),
                request.getDifficulty(),
                request.getDurationMinutes(),
                request.getScheduledStartAt(),
                request.getScheduledEndAt(),
                AssessmentStatus.SCHEDULED
        );

        Assessment saved = assessmentRepository.save(assessment);
        return mapToAssessmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentListItemResponse> getAssessmentsByWorkspace(UUID recruiterId, UUID workspaceId) {
        workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        return assessmentRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(this::mapToListItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(UUID userId, Role role, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        if (role == Role.CANDIDATE && userId != null) {
            if (!assessment.getCandidate().getId().equals(userId)) {
                throw new ForbiddenException("You do not have access to this assessment.", "FORBIDDEN");
            }
        }

        return mapToAssessmentResponse(assessment);
    }

    public AssessmentResponse updateAssessment(UUID recruiterId, UUID assessmentId, UpdateAssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, assessment.getWorkspace().getId());

        if (assessment.getStatus() == AssessmentStatus.IN_PROGRESS ||
            assessment.getStatus() == AssessmentStatus.EVALUATING ||
            assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new ValidationException("Cannot update an assessment that is in progress or completed.", "VALIDATION_ERROR");
        }

        if (request.getRepositoryUrl() != null && !request.getRepositoryUrl().trim().isEmpty()) {
            assessment.setRepositoryUrl(request.getRepositoryUrl().trim());
        }
        if (request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
            assessment.setBranchName(request.getBranchName().trim());
        }
        if (request.getBackendRootDirectory() != null && !request.getBackendRootDirectory().trim().isEmpty()) {
            assessment.setBackendRootDirectory(request.getBackendRootDirectory().trim());
        }
        if (request.getDifficulty() != null) {
            assessment.setDifficulty(request.getDifficulty());
        }
        if (request.getDurationMinutes() != null) {
            assessment.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getScheduledStartAt() != null) {
            assessment.setScheduledStartAt(request.getScheduledStartAt());
        }
        if (request.getScheduledEndAt() != null) {
            assessment.setScheduledEndAt(request.getScheduledEndAt());
        }

        Assessment updated = assessmentRepository.save(assessment);
        return mapToAssessmentResponse(updated);
    }

    public AssessmentResponse cancelAssessment(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, assessment.getWorkspace().getId());

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel an already completed assessment.", "VALIDATION_ERROR");
        }

        assessment.setStatus(AssessmentStatus.CANCELLED);
        Assessment saved = assessmentRepository.save(assessment);
        return mapToAssessmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProcessingStatusResponse getProcessingStatus(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        List<ProcessingStageDto> stages = Arrays.asList(
                new ProcessingStageDto("CLONING", "COMPLETED"),
                new ProcessingStageDto("REPOSITORY_ANALYSIS", "COMPLETED"),
                new ProcessingStageDto("FEATURE_GENERATION", "COMPLETED"),
                new ProcessingStageDto("TEST_GENERATION", "COMPLETED")
        );

        return new ProcessingStatusResponse(
                assessment.getId(),
                assessment.getStatus(),
                stages
        );
    }

    public StartAssessmentResponse startAssessment(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("You do not have access to start this assessment.", "FORBIDDEN");
        }

        if (assessment.getStatus() == AssessmentStatus.CANCELLED) {
            throw new ValidationException("This assessment has been cancelled.", "ASSESSMENT_CANCELLED");
        }
        if (assessment.getStatus() == AssessmentStatus.COMPLETED || assessment.getStatus() == AssessmentStatus.EVALUATING) {
            throw new AssessmentAlreadySubmittedException("Assessment is already completed/submitted.");
        }

        Instant now = Instant.now();
        if (now.isBefore(assessment.getScheduledStartAt()) || now.isAfter(assessment.getScheduledEndAt())) {
            throw new AssessmentNotAvailableException(
                    "Current time is outside the scheduled assessment window (" +
                    assessment.getScheduledStartAt() + " to " + assessment.getScheduledEndAt() + ")"
            );
        }

        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        Assessment saved = assessmentRepository.save(assessment);

        return new StartAssessmentResponse(saved.getId(), saved.getStatus());
    }

    public SubmitAssessmentResponse submitAssessment(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("You do not have access to submit this assessment.", "FORBIDDEN");
        }

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new AssessmentAlreadySubmittedException("Assessment has already been submitted and completed.");
        }

        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setScore(BigDecimal.valueOf(85.00));
        assessmentRepository.save(assessment);

        UUID dummySubmissionId = UUID.randomUUID();
        return new SubmitAssessmentResponse(dummySubmissionId, assessment.getId(), AssessmentStatus.EVALUATING);
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        AssessmentResponse response = new AssessmentResponse();
        response.setAssessmentId(assessment.getId());
        response.setWorkspaceId(assessment.getWorkspace().getId());
        response.setWorkspaceName(assessment.getWorkspace().getName());
        response.setCandidateId(assessment.getCandidate().getId());
        response.setCandidateName(assessment.getCandidate().getName());
        response.setCandidateEmail(assessment.getCandidate().getEmail());
        response.setRepositoryUrl(assessment.getRepositoryUrl());
        response.setBranchName(assessment.getBranchName());
        response.setBackendRootDirectory(assessment.getBackendRootDirectory());
        response.setDifficulty(assessment.getDifficulty());
        response.setDurationMinutes(assessment.getDurationMinutes());
        response.setScheduledStartAt(assessment.getScheduledStartAt());
        response.setScheduledEndAt(assessment.getScheduledEndAt());
        response.setStatus(assessment.getStatus());
        response.setScore(assessment.getScore());
        response.setCreatedAt(assessment.getCreatedAt());
        return response;
    }

    private AssessmentListItemResponse mapToListItemResponse(Assessment assessment) {
        return new AssessmentListItemResponse(
                assessment.getId(),
                assessment.getCandidate().getId(),
                assessment.getCandidate().getName(),
                assessment.getDifficulty(),
                assessment.getDurationMinutes(),
                assessment.getScheduledStartAt(),
                assessment.getScheduledEndAt(),
                assessment.getStatus(),
                assessment.getScore()
        );
    }
}
