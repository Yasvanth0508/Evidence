package com.example.backend.assessment.dto.evaluation;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.workspace.dto.CandidateSummaryDto;
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
public class RecruiterReportResponse {

    private UUID assessmentId;
    private CandidateSummaryDto candidate;
    private BigDecimal score;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private Long timeTakenSeconds;
    private AssessmentStatus status;
    private Instant evaluatedAt;
}
