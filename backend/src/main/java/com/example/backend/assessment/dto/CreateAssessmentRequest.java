package com.example.backend.assessment.dto;

import com.example.backend.common.enums.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    private String title;

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @Builder.Default
    private String backendRootDirectory = "";

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Scheduled start time is required")
    private Instant scheduledStartAt;

    @NotNull(message = "Scheduled end time is required")
    private Instant scheduledEndAt;

    public String getBackendRootDirectory() {
        return backendRootDirectory != null ? backendRootDirectory : "";
    }
}
