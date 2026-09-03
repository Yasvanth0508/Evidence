package com.example.backend.assessment.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegritySummaryDto {
    private String overallRiskBadge;
    private BehaviorSummaryDto behaviorSummary;
    private String riskAnalysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorSummaryDto {
        private Integer copyPasteEvents;
        private Integer buildRuns;
        private Integer testRuns;
        private Integer idleTimeMinutes;
    }
}
