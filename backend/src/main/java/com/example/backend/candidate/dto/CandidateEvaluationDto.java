package com.example.backend.candidate.dto;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEvaluationDto {

    private BigDecimal score;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private Long timeTakenSeconds;
    private SubmissionStatus status;
    private Instant evaluatedAt;
}
