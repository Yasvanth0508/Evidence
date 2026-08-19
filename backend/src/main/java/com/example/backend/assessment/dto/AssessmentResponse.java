package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AssessmentResponse {

    private UUID assessmentId;
    private UUID workspaceId;
    private String workspaceName;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private String repositoryUrl;
    private String branchName;
    private String backendRootDirectory;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private AssessmentStatus status;
    private BigDecimal score;
    private Instant createdAt;

    public AssessmentResponse() {
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Instant getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Instant scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Instant getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Instant scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
