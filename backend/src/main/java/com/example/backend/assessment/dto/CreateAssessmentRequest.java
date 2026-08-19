package com.example.backend.assessment.dto;

import com.example.backend.common.enums.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class CreateAssessmentRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Backend root directory is required")
    private String backendRootDirectory;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Scheduled start time is required")
    private Instant scheduledStartAt;

    @NotNull(message = "Scheduled end time is required")
    private Instant scheduledEndAt;

    public CreateAssessmentRequest() {
    }

    public CreateAssessmentRequest(UUID candidateId, String repositoryUrl, String branchName,
                                   String backendRootDirectory, Difficulty difficulty,
                                   Integer durationMinutes, Instant scheduledStartAt, Instant scheduledEndAt) {
        this.candidateId = candidateId;
        this.repositoryUrl = repositoryUrl;
        this.branchName = branchName;
        this.backendRootDirectory = backendRootDirectory;
        this.difficulty = difficulty;
        this.durationMinutes = durationMinutes;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
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
