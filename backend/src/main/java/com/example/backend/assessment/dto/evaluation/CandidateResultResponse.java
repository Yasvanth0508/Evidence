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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResultResponse {

    private UUID assessmentId;
    private String title;
    private String workspaceName;
    private String difficulty;
    private String techStack;
    private BigDecimal score;
    private String scoreRating;
    private AssessmentStatus status;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private Long timeTakenSeconds;
    private Integer timeTakenMinutes;
    private Instant evaluatedAt;
    private Instant submittedAt;
    private List<CategoryScoreDto> categoryBreakdown;
    private String aiSummary;
    private List<String> strengths;
    private List<String> improvements;
}
