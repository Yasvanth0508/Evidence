package com.example.backend.workspace.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.*;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import com.example.backend.common.enums.WorkspaceStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.UnauthorizedException;
import com.example.backend.workspace.dto.*;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.entity.WorkspaceCandidateId;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing recruiter workspaces, candidate enrollment, and workspace lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final AssessmentWorkspaceRepository assessmentWorkspaceRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final TestResultRepository testResultRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new assessment workspace owned by the authenticated recruiter.
     *
     * @param recruiterId UUID of the authenticated recruiter creating the workspace.
     * @param request     DTO containing workspace name and optional description.
     * @return WorkspaceResponse containing details of the created workspace.
     * @throws UnauthorizedException if recruiter ID is missing or invalid.
     */
    public WorkspaceResponse createWorkspace(UUID recruiterId, CreateWorkspaceRequest request) {
        log.info("Creating workspace '{}' for recruiterId: {}", request.getName(), recruiterId);
        User recruiter = getRecruiterOrThrow(recruiterId);

        Workspace workspace = Workspace.builder()
                .recruiter(recruiter)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(WorkspaceStatus.ACTIVE)
                .build();

        Workspace saved = workspaceRepository.save(workspace);
        log.info("Workspace created successfully with ID: {}", saved.getId());
        return mapToWorkspaceResponse(saved);
    }

    /**
     * Retrieves all workspaces belonging to the authenticated recruiter.
     *
     * @param recruiterId UUID of the recruiter.
     * @return List of WorkspaceResponse objects.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspaces(UUID recruiterId) {
        User recruiter = getRecruiterOrThrow(recruiterId);
        return workspaceRepository.findAllByRecruiterId(recruiter.getId()).stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves detailed workspace information, including enrolled candidates and assessment count.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace to fetch.
     * @return WorkspaceDetailResponse with enrolled candidate summaries and statistics.
     * @throws ResourceNotFoundException if workspace does not exist.
     * @throws ForbiddenException        if recruiter does not own the workspace.
     */
    @Transactional(readOnly = true)
    public WorkspaceDetailResponse getWorkspaceById(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        List<CandidateSummaryDto> candidates = workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(wc -> CandidateSummaryDto.builder()
                        .id(wc.getCandidate().getId())
                        .name(wc.getCandidate().getName())
                        .email(wc.getCandidate().getEmail())
                        .addedAt(wc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        long assessmentCount = assessmentRepository.countByWorkspaceId(workspace.getId());

        return WorkspaceDetailResponse.builder()
                .id(workspace.getId())
                .recruiterId(workspace.getRecruiter().getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .status(workspace.getStatus())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .candidates(candidates)
                .assessmentCount(assessmentCount)
                .build();
    }

    /**
     * Updates an existing workspace's title, description, or status.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace to update.
     * @param request     DTO with fields to update.
     * @return Updated WorkspaceResponse.
     */
    public WorkspaceResponse updateWorkspace(UUID recruiterId, UUID workspaceId, UpdateWorkspaceRequest request) {
        log.info("Updating workspace ID: {}", workspaceId);
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            workspace.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            workspace.setStatus(request.getStatus());
        }

        Workspace updated = workspaceRepository.save(workspace);
        return mapToWorkspaceResponse(updated);
    }

    /**
     * Archives a workspace, preventing new assessments from being scheduled.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace to archive.
     * @return Updated WorkspaceResponse with ARCHIVED status.
     */
    public WorkspaceResponse archiveWorkspace(UUID recruiterId, UUID workspaceId) {
        log.info("Archiving workspace ID: {}", workspaceId);
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);
        workspace.setStatus(WorkspaceStatus.ARCHIVED);
        Workspace saved = workspaceRepository.save(workspace);
        return mapToWorkspaceResponse(saved);
    }

    /**
     * Permanently deletes a workspace and cascades deletion across all related assessments and test results.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace to delete.
     */
    public void deleteWorkspace(UUID recruiterId, UUID workspaceId) {
        log.info("Deleting workspace ID: {}", workspaceId);
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        List<Assessment> assessments = assessmentRepository.findAllByWorkspaceId(workspace.getId());
        for (Assessment a : assessments) {
            var testResults = testResultRepository.findAllByAssessmentId(a.getId());
            testResultRepository.deleteAll(testResults);

            var testCases = testCaseRepository.findAllByAssessmentIdOrderByTestCaseNumberAsc(a.getId());
            testCaseRepository.deleteAll(testCases);

            evaluationReportRepository.findBySubmissionId(a.getId()).ifPresent(evaluationReportRepository::delete);
            submissionRepository.findByAssessmentId(a.getId()).ifPresent(submissionRepository::delete);

            repositoryAnalysisRepository.findByAssessmentId(a.getId()).ifPresent(repositoryAnalysisRepository::delete);
            assessmentWorkspaceRepository.findByAssessmentId(a.getId()).ifPresent(assessmentWorkspaceRepository::delete);
            featureSpecificationRepository.findByAssessmentId(a.getId()).ifPresent(featureSpecificationRepository::delete);
        }
        assessmentRepository.deleteAll(assessments);

        List<WorkspaceCandidate> candidates = workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId());
        workspaceCandidateRepository.deleteAll(candidates);

        workspaceRepository.delete(workspace);
        log.info("Workspace ID: {} and all nested entities successfully deleted", workspaceId);
    }

    /**
     * Lists all candidates currently enrolled in a specific workspace.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace.
     * @return List of WorkspaceCandidateItemResponse records.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceCandidateItemResponse> getCandidatesInWorkspace(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        return workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(wc -> WorkspaceCandidateItemResponse.builder()
                        .workspaceId(workspace.getId())
                        .candidate(CandidateDto.builder()
                                .id(wc.getCandidate().getId())
                                .name(wc.getCandidate().getName())
                                .email(wc.getCandidate().getEmail())
                                .role(wc.getCandidate().getRole())
                                .build())
                        .createdAt(wc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Enrolls a candidate into a workspace by email, creating a user account if not yet registered.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace.
     * @param request     DTO with candidate email and name.
     * @return WorkspaceCandidateItemResponse containing enrolled candidate details.
     */
    public WorkspaceCandidateItemResponse addCandidateToWorkspace(UUID recruiterId, UUID workspaceId, AddCandidateRequest request) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        String email = request.getEmail().trim().toLowerCase();
        log.info("Enrolling candidate '{}' into workspace ID: {}", email, workspaceId);

        User candidate = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String candidateName = request.getName() != null && !request.getName().trim().isEmpty()
                            ? request.getName().trim()
                            : email.split("@")[0];
                    // Secure random initial password hash for invited candidates
                    String secureInitPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    User newCandidate = User.builder()
                            .name(candidateName)
                            .email(email)
                            .passwordHash(secureInitPassword)
                            .role(Role.CANDIDATE)
                            .authProvider(AuthProvider.LOCAL)
                            .build();
                    return userRepository.save(newCandidate);
                });

        if (workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            return WorkspaceCandidateItemResponse.builder()
                    .workspaceId(workspace.getId())
                    .candidate(CandidateDto.builder()
                            .id(candidate.getId())
                            .name(candidate.getName())
                            .email(candidate.getEmail())
                            .role(candidate.getRole())
                            .build())
                    .createdAt(workspace.getCreatedAt())
                    .build();
        }

        WorkspaceCandidate workspaceCandidate = WorkspaceCandidate.builder()
                .workspace(workspace)
                .candidate(candidate)
                .id(new WorkspaceCandidateId(workspace.getId(), candidate.getId()))
                .build();
        WorkspaceCandidate saved = workspaceCandidateRepository.save(workspaceCandidate);

        return WorkspaceCandidateItemResponse.builder()
                .workspaceId(workspace.getId())
                .candidate(CandidateDto.builder()
                        .id(candidate.getId())
                        .name(candidate.getName())
                        .email(candidate.getEmail())
                        .role(candidate.getRole())
                        .build())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Removes an enrolled candidate from a workspace.
     *
     * @param recruiterId UUID of the authenticated recruiter.
     * @param workspaceId UUID of the workspace.
     * @param candidateId UUID of the candidate to remove.
     */
    public void removeCandidateFromWorkspace(UUID recruiterId, UUID workspaceId, UUID candidateId) {
        log.info("Removing candidate ID: {} from workspace ID: {}", candidateId, workspaceId);
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        WorkspaceCandidate workspaceCandidate = workspaceCandidateRepository
                .findByWorkspaceIdAndCandidateId(workspace.getId(), candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate is not enrolled in this workspace.",
                        "CANDIDATE_NOT_FOUND"
                ));

        workspaceCandidateRepository.delete(workspaceCandidate);
    }

    /**
     * Verifies that a workspace exists and that the calling recruiter owns it.
     *
     * @param recruiterId UUID of the recruiter.
     * @param workspaceId UUID of the workspace.
     * @return The verified Workspace entity.
     * @throws ResourceNotFoundException if the workspace is not found.
     * @throws ForbiddenException        if the workspace is owned by a different recruiter.
     */
    public Workspace getWorkspaceAndVerifyOwnership(UUID recruiterId, UUID workspaceId) {
        if (workspaceId == null) {
            throw new ResourceNotFoundException("Workspace ID is required.");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        if (workspace.getRecruiter() == null || !workspace.getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to manage this workspace.");
        }

        return workspace;
    }

    /**
     * Retrieves the recruiter user entity or throws an UnauthorizedException if not found.
     *
     * @param recruiterId UUID of the recruiter.
     * @return User entity with Role.RECRUITER.
     * @throws UnauthorizedException if recruiter does not exist in the database.
     */
    public User getRecruiterOrThrow(UUID recruiterId) {
        if (recruiterId == null) {
            throw new UnauthorizedException("Recruiter authentication required.");
        }
        return userRepository.findById(recruiterId)
                .filter(u -> u.getRole() == Role.RECRUITER)
                .orElseThrow(() -> new UnauthorizedException("Recruiter account not found or invalid for ID: " + recruiterId));
    }

    private WorkspaceResponse mapToWorkspaceResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .recruiterId(workspace.getRecruiter().getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .status(workspace.getStatus())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
