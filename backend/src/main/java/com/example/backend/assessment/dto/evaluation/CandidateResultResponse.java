package com.example.backend.assessment.dto.evaluation;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.BuildStatus;
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
public class CandidateResultResponse {

    private UUID assessmentId;
    private BigDecimal score;
    private AssessmentStatus status;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private Long timeTakenSeconds;
    private Instant evaluatedAt;
    private Instant submittedAt;
}
