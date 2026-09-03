package com.example.backend.dashboard.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedAssessmentDto {

    private UUID assessmentId;

    @JsonProperty("id")
    public UUID getId() {
        return assessmentId;
    }

    private String title;
    private UUID workspaceId;
    private String workspaceName;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant submittedAt;

    @JsonProperty("completedAt")
    public Instant getCompletedAt() {
        return submittedAt;
    }

    private Long timeTakenSeconds;
    private BigDecimal score;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private AssessmentStatus status;
}
