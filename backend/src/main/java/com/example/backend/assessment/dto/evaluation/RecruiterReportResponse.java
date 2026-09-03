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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterReportResponse {

    private UUID assessmentId;
    private String title;
    private UUID workspaceId;
    private String workspaceName;
    private String difficulty;
    private String techStack;
    private CandidateSummaryDto candidate;
    private BigDecimal score;
    private String scoreRating;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private Long timeTakenSeconds;
    private Integer timeTakenMinutes;
    private AssessmentStatus status;
    private Instant evaluatedAt;
    private Instant submittedAt;
    private List<CategoryScoreDto> categoryBreakdown;
    private String aiSummary;
    private List<String> strengths;
    private List<String> improvements;
    private IntegritySummaryDto integrity;
}
