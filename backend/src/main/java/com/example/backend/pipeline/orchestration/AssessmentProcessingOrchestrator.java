package com.example.backend.pipeline.orchestration;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.pipeline.analysis.AstAnalysisService;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.docker.DockerValidationService;
import com.example.backend.pipeline.docker.dto.DockerValidationResult;
import com.example.backend.pipeline.feature.FeatureSpecificationService;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.git.GitCloningService;
import com.example.backend.pipeline.git.dto.GitCloneResult;
import com.example.backend.pipeline.orchestration.dto.PipelineExecutionResult;
import com.example.backend.pipeline.testcase.TestCaseGenerationService;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AssessmentProcessingOrchestrator {

    private final GitCloningService gitCloningService;
    private final DockerValidationService dockerValidationService;
    private final AstAnalysisService astAnalysisService;
    private final FeatureSpecificationService featureSpecificationService;
    private final TestCaseGenerationService testCaseGenerationService;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public AssessmentProcessingOrchestrator(GitCloningService gitCloningService,
                                            DockerValidationService dockerValidationService,
                                            AstAnalysisService astAnalysisService,
                                            FeatureSpecificationService featureSpecificationService,
                                            TestCaseGenerationService testCaseGenerationService,
                                            AssessmentRepository assessmentRepository) {
        this.gitCloningService = gitCloningService;
        this.dockerValidationService = dockerValidationService;
        this.astAnalysisService = astAnalysisService;
        this.featureSpecificationService = featureSpecificationService;
        this.testCaseGenerationService = testCaseGenerationService;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Executes the complete 5-phase pipeline, writes visible inspection artifacts to disk,
     * and persists all metadata to PostgreSQL.
     */
    @Transactional
    public PipelineExecutionResult executePipeline(UUID assessmentId, String repoUrl, String branch, String backendRootDir) {
        long startTime = System.currentTimeMillis();
        log.info("===================================================================");
        log.info("STARTING END-TO-END PIPELINE FOR ASSESSMENT ID: {}", assessmentId);
        log.info("Repository: {} | Branch: {} | Subdir: {}", repoUrl, branch, backendRootDir);
        log.info("===================================================================");

        PipelineExecutionResult result = new PipelineExecutionResult();
        result.setAssessmentId(assessmentId);
        result.setRepositoryUrl(repoUrl);
        result.setBranchName(branch != null ? branch : "main");
        result.setBackendRootDirectory(backendRootDir != null ? backendRootDir : "");

        // Create dedicated visible inspection directory on disk
        Path inspectionDir = Paths.get(System.getProperty("user.dir"), "storage", "pipeline_runs", assessmentId.toString());
        try {
            Files.createDirectories(inspectionDir);
            result.setInspectionFolderPath(inspectionDir.toAbsolutePath().toString());
        } catch (IOException e) {
            log.warn("Could not create inspection directory {}: {}", inspectionDir, e.getMessage());
        }

        updateAssessmentStatus(assessmentId, AssessmentStatus.CREATING);

        // -------------------------------------------------------------------
        // PHASE 1: Git Repository Cloning & Ingestion
        // -------------------------------------------------------------------
        log.info(">>> Executing Phase 1: Git Repository Ingestion...");
        GitCloneResult gitResult = gitCloningService.cloneRepository(assessmentId, repoUrl, branch);
        result.setGitCloneResult(gitResult);

        if (!gitResult.isSuccess()) {
            updateAssessmentStatus(assessmentId, AssessmentStatus.FAILED);
            result.setSuccess(false);
            result.setErrorMessage("Phase 1 Git Clone Failed: " + gitResult.getErrorMessage());
            saveInspectionJson(inspectionDir, "01_git_clone_result.json", gitResult);
            return result;
        }
        saveInspectionJson(inspectionDir, "01_git_clone_result.json", gitResult);

        Path repoRootPath = Paths.get(gitResult.getLocalRepositoryPath());

        // -------------------------------------------------------------------
        // PHASE 2: Docker Build & Runnable Validation
        // -------------------------------------------------------------------
        updateAssessmentStatus(assessmentId, AssessmentStatus.ANALYZING);
        log.info(">>> Executing Phase 2: Docker Build & Validation...");
        DockerValidationResult dockerResult = dockerValidationService.validateProjectRunnable(assessmentId, repoRootPath, backendRootDir);
        result.setDockerValidationResult(dockerResult);
        saveInspectionJson(inspectionDir, "02_docker_validation_result.json", dockerResult);

        // -------------------------------------------------------------------
        // PHASE 3: AST Codebase Metadata Extraction
        // -------------------------------------------------------------------
        log.info(">>> Executing Phase 3: AST Codebase Extraction...");
        AstAnalysisResult astResult = astAnalysisService.analyzeSourceCode(assessmentId, repoRootPath, backendRootDir);
        result.setAstAnalysisResult(astResult);

        if (!astResult.isSuccess()) {
            updateAssessmentStatus(assessmentId, AssessmentStatus.FAILED);
            result.setSuccess(false);
            result.setErrorMessage("Phase 3 AST Analysis Failed: " + astResult.getErrorMessage());
            return result;
        }

        saveInspectionJson(inspectionDir, "03_project_structure.json", astResult.getProjectStructure());
        saveInspectionJson(inspectionDir, "03_source_code_structure.json", astResult.getSourceCodeStructure());
        saveInspectionJson(inspectionDir, "03_content_details.json", astResult.getContentDetails());

        // -------------------------------------------------------------------
        // PHASE 4: AI Feature Specification Generation
        // -------------------------------------------------------------------
        updateAssessmentStatus(assessmentId, AssessmentStatus.GENERATING_FEATURE);
        log.info(">>> Executing Phase 4: AI Feature Specification Generation...");
        FeatureGenerationResult featureResult = featureSpecificationService.generateFeatureSpecification(assessmentId, astResult);
        result.setFeatureGenerationResult(featureResult);

        if (!featureResult.isSuccess()) {
            updateAssessmentStatus(assessmentId, AssessmentStatus.FAILED);
            result.setSuccess(false);
            result.setErrorMessage("Phase 4 Feature Generation Failed: " + featureResult.getErrorMessage());
            return result;
        }

        saveInspectionJson(inspectionDir, "04_feature_specification.json", featureResult);
        saveInspectionMarkdown(inspectionDir, "04_feature_specification.md", generateFeatureMarkdown(featureResult));

        // -------------------------------------------------------------------
        // PHASE 5: AI Black-Box Test Case Generation
        // -------------------------------------------------------------------
        updateAssessmentStatus(assessmentId, AssessmentStatus.GENERATING_TESTS);
        log.info(">>> Executing Phase 5: AI Black-Box Test Case Generation...");
        TestCaseGenerationResult testCaseResult = testCaseGenerationService.generateTestCases(assessmentId, featureResult);
        result.setTestCaseGenerationResult(testCaseResult);

        if (!testCaseResult.isSuccess()) {
            updateAssessmentStatus(assessmentId, AssessmentStatus.FAILED);
            result.setSuccess(false);
            result.setErrorMessage("Phase 5 Test Case Generation Failed: " + testCaseResult.getErrorMessage());
            return result;
        }

        saveInspectionJson(inspectionDir, "05_test_cases.json", testCaseResult);
        saveInspectionMarkdown(inspectionDir, "05_test_cases_summary.md", generateTestCaseMarkdown(testCaseResult));

        // -------------------------------------------------------------------
        // FINALIZATION & SUMMARY
        // -------------------------------------------------------------------
        long duration = System.currentTimeMillis() - startTime;
        result.setTotalDurationMs(duration);
        result.setSuccess(true);

        updateAssessmentStatus(assessmentId, AssessmentStatus.READY);

        Map<String, Object> summary = new HashMap<>();
        summary.put("assessmentId", assessmentId.toString());
        summary.put("repositoryUrl", repoUrl);
        summary.put("branch", branch);
        summary.put("backendDirectory", backendRootDir);
        summary.put("status", "SUCCESS");
        summary.put("totalDurationMs", duration);
        summary.put("phase1_git_files", gitResult.getTotalFiles());
        summary.put("phase1_commit", gitResult.getCommitHash());
        summary.put("phase2_docker_build", dockerResult.getBuildStatus());
        summary.put("phase2_docker_app", dockerResult.getApplicationStatus());
        summary.put("phase3_controllers", astResult.getSourceCodeStructure().getControllers().size());
        summary.put("phase3_entities", astResult.getSourceCodeStructure().getEntities().size());
        summary.put("phase3_endpoints", astResult.getContentDetails().getTotalEndpoints());
        summary.put("phase4_feature", featureResult.getFeatureName());
        summary.put("phase5_total_test_cases", testCaseResult.getTotalTestCases());

        saveInspectionJson(inspectionDir, "00_pipeline_execution_summary.json", summary);
        saveInspectionMarkdown(inspectionDir, "README_OUTCOME.md", generateReadmeOutcome(result, summary));

        log.info("===================================================================");
        log.info("PIPELINE COMPLETED SUCCESSFULLY IN {} ms", duration);
        log.info("Visible Outcome Artifacts stored at: {}", inspectionDir.toAbsolutePath());
        log.info("===================================================================");

        return result;
    }

    private void updateAssessmentStatus(UUID assessmentId, AssessmentStatus status) {
        if (assessmentRepository == null || assessmentId == null) return;
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
            if (assessment != null && status != null) {
                assessment.setStatus(status);
                assessmentRepository.save(assessment);
            }
        } catch (Exception ex) {
            log.warn("Could not update assessment status: {}", ex.getMessage());
        }
    }

    private void saveInspectionJson(Path dir, String filename, Object data) {
        if (dir == null || data == null) return;
        try {
            Path file = dir.resolve(filename);
            objectMapper.writeValue(file.toFile(), data);
        } catch (Exception ex) {
            log.warn("Could not write inspection file {}: {}", filename, ex.getMessage());
        }
    }

    private void saveInspectionMarkdown(Path dir, String filename, String content) {
        if (dir == null || content == null) return;
        try {
            Path file = dir.resolve(filename);
            Files.writeString(file, content);
        } catch (Exception ex) {
            log.warn("Could not write markdown file {}: {}", filename, ex.getMessage());
        }
    }

    private String generateFeatureMarkdown(FeatureGenerationResult f) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Feature Specification: ").append(f.getFeatureName()).append("\n\n");
        sb.append("## Description\n").append(f.getDescription()).append("\n\n");
        sb.append("## Technical Requirements\n").append(f.getRequirements()).append("\n\n");

        String reqSpec = f.getRequestSpecification();
        sb.append("## Request Specification\n");
        if (reqSpec != null && !reqSpec.trim().isEmpty()) {
            if (reqSpec.trim().startsWith("{") || reqSpec.trim().startsWith("[")) {
                sb.append("```json\n").append(reqSpec.trim()).append("\n```\n\n");
            } else if (reqSpec.contains("```")) {
                sb.append(reqSpec.trim()).append("\n\n");
            } else {
                sb.append("```\n").append(reqSpec.trim()).append("\n```\n\n");
            }
        } else {
            sb.append("(No request specification provided)\n\n");
        }

        String respSpec = f.getResponseSpecification();
        sb.append("## Response Specification\n");
        if (respSpec != null && !respSpec.trim().isEmpty()) {
            if (respSpec.trim().startsWith("{") || respSpec.trim().startsWith("[")) {
                sb.append("```json\n").append(respSpec.trim()).append("\n```\n\n");
            } else if (respSpec.contains("```")) {
                sb.append(respSpec.trim()).append("\n\n");
            } else {
                sb.append("```\n").append(respSpec.trim()).append("\n```\n\n");
            }
        } else {
            sb.append("(No response specification provided)\n\n");
        }

        sb.append("## Constraints\n").append(f.getConstraints()).append("\n");
        return sb.toString();
    }

    private String generateTestCaseMarkdown(TestCaseGenerationResult t) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated Black-Box Test Cases (Total: ").append(t.getTotalTestCases()).append(")\n\n");
        for (TestCaseItemDto tc : t.getTestCases()) {
            sb.append("### Test Case #").append(tc.getTestCaseNumber()).append(": ").append(tc.getDescription()).append("\n");
            sb.append("- **Test Type:** `").append(tc.getTestType()).append("`\n");
            sb.append("- **Weight:** ").append(tc.getWeight()).append("\n");
            sb.append("- **HTTP Call:** `").append(tc.getHttpMethod()).append(" ").append(tc.getEndpoint()).append("`\n");
            if (tc.getRequestData() != null) {
                sb.append("- **Request Payload:**\n```json\n").append(tc.getRequestData()).append("\n```\n");
            }
            sb.append("- **Expected Status Code:** `").append(tc.getExpectedStatusCode()).append("`\n");
            if (tc.getExpectedResponse() != null) {
                sb.append("- **Expected Response:**\n```json\n").append(tc.getExpectedResponse()).append("\n```\n");
            }
            sb.append("- **Assertions:** `").append(tc.getAssertions()).append("`\n\n---\n\n");
        }
        return sb.toString();
    }

    private String generateReadmeOutcome(PipelineExecutionResult r, Map<String, Object> summary) {
        return "# Pipeline Execution Outcome Report\n\n" +
                "- **Assessment ID:** `" + r.getAssessmentId() + "`\n" +
                "- **Repository URL:** " + r.getRepositoryUrl() + "\n" +
                "- **Branch:** " + r.getBranchName() + "\n" +
                "- **Backend Subdirectory:** " + r.getBackendRootDirectory() + "\n" +
                "- **Execution Time:** " + r.getTotalDurationMs() + " ms\n\n" +
                "## Generated Artifacts in this Folder\n" +
                "1. `01_git_clone_result.json` - Phase 1 Clone metadata and commit hash\n" +
                "2. `02_docker_validation_result.json` - Phase 2 Docker build & runnable logs\n" +
                "3. `03_project_structure.json` - Phase 3 Maven & dependency metadata\n" +
                "4. `03_source_code_structure.json` - Phase 3 AST extracted controllers, entities, repositories\n" +
                "5. `03_content_details.json` - Phase 3 Aggregated metrics and routes\n" +
                "6. `04_feature_specification.md` / `.json` - Phase 4 AI Feature Specification\n" +
                "7. `05_test_cases_summary.md` / `.json` - Phase 5 Black-Box Test Cases\n" +
                "8. `00_pipeline_execution_summary.json` - Complete execution summary\n";
    }
}
