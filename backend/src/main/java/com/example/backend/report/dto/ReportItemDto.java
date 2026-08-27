package com.example.backend.report.dto;

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
public class ReportItemDto {

    private UUID assessmentId;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private String workspaceName;
    private BigDecimal score;
    private AssessmentStatus status;
    private Instant submittedAt;
}
