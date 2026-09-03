package com.example.backend.workspace.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.*;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceDeletionService {

    private final WorkspaceService workspaceService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final AssessmentWorkspaceRepository assessmentWorkspaceRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final TestResultRepository testResultRepository;

    public void deleteWorkspace(UUID recruiterId, UUID workspaceId) {
        log.info("Deleting workspace ID: {}", workspaceId);
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

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
}
