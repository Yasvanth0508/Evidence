package com.example.backend.assessment.dto;

import com.example.backend.common.enums.Difficulty;

import java.time.Instant;

public class UpdateAssessmentRequest {

    private String repositoryUrl;
    private String branchName;
    private String backendRootDirectory;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;

    public UpdateAssessmentRequest() {
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
}
