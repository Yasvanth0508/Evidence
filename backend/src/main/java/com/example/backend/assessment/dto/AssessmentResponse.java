package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
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
public class AssessmentResponse {

    private UUID assessmentId;

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public UUID getId() {
        return assessmentId;
    }

    private UUID workspaceId;
    private UUID candidateId;
    private String title;
    private String repositoryUrl;
    private String branchName;
    private String backendRootDirectory;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private AssessmentStatus status;
}
