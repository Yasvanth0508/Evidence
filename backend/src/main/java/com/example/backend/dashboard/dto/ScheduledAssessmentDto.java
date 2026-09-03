package com.example.backend.dashboard.dto;

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
public class ScheduledAssessmentDto {

    private UUID assessmentId;

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public UUID getId() {
        return assessmentId;
    }

    private String title;
    private UUID workspaceId;
    private String workspaceName;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private AssessmentStatus status;
}
