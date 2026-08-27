package com.example.backend.pipeline.orchestration.dto;

import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.docker.dto.DockerValidationResult;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.git.dto.GitCloneResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;

import java.util.UUID;

public class PipelineExecutionResult {

    private boolean success;
    private UUID assessmentId;
    private String repositoryUrl;
    private String branchName;
    private String backendRootDirectory;
    private String inspectionFolderPath;
    private long totalDurationMs;

    private GitCloneResult gitCloneResult;
    private DockerValidationResult dockerValidationResult;
    private AstAnalysisResult astAnalysisResult;
    private FeatureGenerationResult featureGenerationResult;
    private TestCaseGenerationResult testCaseGenerationResult;

    private String errorMessage;

    public PipelineExecutionResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBackendRootDirectory() {
        return backendRootDirectory;
    }

    public void setBackendRootDirectory(String backendRootDirectory) {
        this.backendRootDirectory = backendRootDirectory;
    }

    public String getInspectionFolderPath() {
        return inspectionFolderPath;
    }

    public void setInspectionFolderPath(String inspectionFolderPath) {
        this.inspectionFolderPath = inspectionFolderPath;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public GitCloneResult getGitCloneResult() {
        return gitCloneResult;
    }

    public void setGitCloneResult(GitCloneResult gitCloneResult) {
        this.gitCloneResult = gitCloneResult;
    }

    public DockerValidationResult getDockerValidationResult() {
        return dockerValidationResult;
    }

    public void setDockerValidationResult(DockerValidationResult dockerValidationResult) {
        this.dockerValidationResult = dockerValidationResult;
    }

    public AstAnalysisResult getAstAnalysisResult() {
        return astAnalysisResult;
    }

    public void setAstAnalysisResult(AstAnalysisResult astAnalysisResult) {
        this.astAnalysisResult = astAnalysisResult;
    }

    public FeatureGenerationResult getFeatureGenerationResult() {
        return featureGenerationResult;
    }

    public void setFeatureGenerationResult(FeatureGenerationResult featureGenerationResult) {
        this.featureGenerationResult = featureGenerationResult;
    }

    public TestCaseGenerationResult getTestCaseGenerationResult() {
        return testCaseGenerationResult;
    }

    public void setTestCaseGenerationResult(TestCaseGenerationResult testCaseGenerationResult) {
        this.testCaseGenerationResult = testCaseGenerationResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
