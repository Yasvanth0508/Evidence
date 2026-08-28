package com.example.backend.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Aggregated summary analytics DTO for reports dashboard and KPIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {
    private long totalCandidates;
    private long completedAssessments;
    private long scheduledAssessments;
    private int participationRate;
    private long passedAssessments;
    private int passRate;
    private BigDecimal averageScore;
}
