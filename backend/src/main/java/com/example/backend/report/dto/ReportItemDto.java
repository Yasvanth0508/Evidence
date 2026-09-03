package com.example.backend.report.dto;

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
public class ReportItemDto {

    private UUID assessmentId;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private UUID workspaceId;
    private String workspaceName;
    private Difficulty difficulty;
    private BigDecimal score;
    private AssessmentStatus status;
    private Instant submittedAt;

    @JsonProperty("completedAt")
    public Instant getCompletedAt() {
        return submittedAt;
    }
}
