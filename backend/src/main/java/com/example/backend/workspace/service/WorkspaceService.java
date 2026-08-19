package com.example.backend.workspace.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.Role;
import com.example.backend.common.enums.WorkspaceStatus;
import com.example.backend.common.exception.DuplicateResourceException;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.analysis.repository.RepositoryAnalysisRepository;
import com.example.backend.analysis.repository.RepositoryRecordRepository;
import com.example.backend.evaluation.repository.SubmissionRepository;
import com.example.backend.evaluation.repository.TestCaseRepository;
import com.example.backend.evaluation.repository.TestResultRepository;
import com.example.backend.execution.repository.ExecutionRepository;
import com.example.backend.feature.repository.FeatureSpecificationRepository;
import com.example.backend.selectedcandidate.repository.SelectedCandidateRepository;
import com.example.backend.workspace.dto.*;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final SelectedCandidateRepository selectedCandidateRepository;
    private final ExecutionRepository executionRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestResultRepository testResultRepository;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final RepositoryRecordRepository repositoryRecordRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceCandidateRepository workspaceCandidateRepository,
                            UserRepository userRepository,
                            AssessmentRepository assessmentRepository,
                            SelectedCandidateRepository selectedCandidateRepository,
                            ExecutionRepository executionRepository,
                            SubmissionRepository submissionRepository,
                            TestCaseRepository testCaseRepository,
                            TestResultRepository testResultRepository,
                            RepositoryAnalysisRepository repositoryAnalysisRepository,
                            RepositoryRecordRepository repositoryRecordRepository,
                            FeatureSpecificationRepository featureSpecificationRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceCandidateRepository = workspaceCandidateRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.selectedCandidateRepository = selectedCandidateRepository;
        this.executionRepository = executionRepository;
        this.submissionRepository = submissionRepository;
        this.testCaseRepository = testCaseRepository;
        this.testResultRepository = testResultRepository;
        this.repositoryAnalysisRepository = repositoryAnalysisRepository;
        this.repositoryRecordRepository = repositoryRecordRepository;
        this.featureSpecificationRepository = featureSpecificationRepository;
    }

    public WorkspaceResponse createWorkspace(UUID recruiterId, CreateWorkspaceRequest request) {
        User recruiter = getOrCreateRecruiter(recruiterId);

        Workspace workspace = new Workspace(
                recruiter,
                request.getName(),
                request.getDescription(),
                WorkspaceStatus.ACTIVE
        );

        Workspace saved = workspaceRepository.save(workspace);
        return mapToWorkspaceResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspaces(UUID recruiterId) {
        User recruiter = getOrCreateRecruiter(recruiterId);
        return workspaceRepository.findAllByRecruiterId(recruiter.getId()).stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);
        return mapToWorkspaceResponse(workspace);
    }

    public WorkspaceResponse updateWorkspace(UUID recruiterId, UUID workspaceId, UpdateWorkspaceRequest request) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            workspace.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription().trim());
        }

        Workspace updated = workspaceRepository.save(workspace);
        return mapToWorkspaceResponse(updated);
    }

    public void deleteWorkspace(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        // Clean up selected candidates
        var selectedCandidates = selectedCandidateRepository.findAllByWorkspaceId(workspace.getId());
        selectedCandidateRepository.deleteAll(selectedCandidates);

        // Clean up associated assessments and their children
        List<Assessment> assessments = assessmentRepository.findAllByWorkspaceId(workspace.getId());
        for (Assessment a : assessments) {
            var testResults = testResultRepository.findAllByAssessmentId(a.getId());
            testResultRepository.deleteAll(testResults);

            var testCases = testCaseRepository.findAllByAssessmentIdOrderByTestCaseNumberAsc(a.getId());
            testCaseRepository.deleteAll(testCases);

            var executions = executionRepository.findAllByAssessmentId(a.getId());
            executionRepository.deleteAll(executions);

            var submissions = submissionRepository.findAllByAssessmentId(a.getId());
            submissionRepository.deleteAll(submissions);

            repositoryAnalysisRepository.findByAssessmentId(a.getId()).ifPresent(repositoryAnalysisRepository::delete);
            repositoryRecordRepository.findByAssessmentId(a.getId()).ifPresent(repositoryRecordRepository::delete);
            featureSpecificationRepository.findByAssessmentId(a.getId()).ifPresent(featureSpecificationRepository::delete);
        }
        assessmentRepository.deleteAll(assessments);

        // Clean up workspace candidate enrollments
        List<WorkspaceCandidate> candidates = workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId());
        workspaceCandidateRepository.deleteAll(candidates);

        workspaceRepository.delete(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceCandidateResponse> getCandidatesInWorkspace(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        return workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(wc -> new WorkspaceCandidateResponse(
                        wc.getCandidate().getId(),
                        wc.getCandidate().getName(),
                        wc.getCandidate().getEmail()
                ))
                .collect(Collectors.toList());
    }

    public AddCandidateResponse addCandidateToWorkspace(UUID recruiterId, UUID workspaceId, AddCandidateToWorkspaceRequest request) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        String email = request.getEmail().trim().toLowerCase();
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate has not registered yet. Ask the candidate to create an account.",
                        "CANDIDATE_NOT_FOUND"
                ));

        if (workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            throw new DuplicateResourceException(
                    "Candidate is already enrolled in this workspace.",
                    "DUPLICATE_RESOURCE"
            );
        }

        WorkspaceCandidate workspaceCandidate = new WorkspaceCandidate(workspace, candidate);
        workspaceCandidateRepository.save(workspaceCandidate);

        return new AddCandidateResponse(
                workspace.getId(),
                new WorkspaceCandidateResponse(candidate.getId(), candidate.getName(), candidate.getEmail())
        );
    }

    public void removeCandidateFromWorkspace(UUID recruiterId, UUID workspaceId, UUID candidateId) {
        Workspace workspace = getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        WorkspaceCandidate workspaceCandidate = workspaceCandidateRepository
                .findByWorkspaceIdAndCandidateId(workspace.getId(), candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate is not enrolled in this workspace.",
                        "CANDIDATE_NOT_FOUND"
                ));

        selectedCandidateRepository.findByWorkspaceIdAndCandidateId(workspace.getId(), candidateId)
                .ifPresent(selectedCandidateRepository::delete);

        workspaceCandidateRepository.delete(workspaceCandidate);
    }

    /**
     * Helper to retrieve or create a mock/default recruiter user for dev testing
     * when auth is disabled.
     */
    public User getOrCreateRecruiter(UUID recruiterId) {
        if (recruiterId != null) {
            return userRepository.findById(recruiterId)
                    .orElseGet(() -> createDefaultRecruiter(recruiterId));
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.RECRUITER)
                .findFirst()
                .orElseGet(() -> createDefaultRecruiter(null));
    }

    private User createDefaultRecruiter(UUID designatedId) {
        User defaultRecruiter = new User(
                "Demo Recruiter",
                "recruiter@example.com",
                "$2a$10$defaultMockPasswordHashForDevOnly",
                Role.RECRUITER
        );
        if (designatedId != null) {
            defaultRecruiter.setId(designatedId);
        }
        return userRepository.save(defaultRecruiter);
    }

    public Workspace getWorkspaceAndVerifyOwnership(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found", "WORKSPACE_NOT_FOUND"));

        if (recruiterId != null && !workspace.getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("You do not have access to this workspace.", "FORBIDDEN");
        }

        return workspace;
    }

    private WorkspaceResponse mapToWorkspaceResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getStatus()
        );
    }
}
