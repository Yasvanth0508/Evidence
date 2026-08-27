package com.example.backend.dashboard.dto;

import com.example.backend.common.enums.AssessmentStatus;
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
    private UUID workspaceId;
    private String workspaceName;
    private Instant submittedAt;
    private Long timeTakenSeconds;
    private BigDecimal score;
    private AssessmentStatus status;
}
