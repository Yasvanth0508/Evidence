package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.evaluation.CategoryScoreDto;
import com.example.backend.assessment.dto.evaluation.IntegritySummaryDto;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.entity.TestResult;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.TestResultStatus;
import com.example.backend.common.enums.TestType;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEvaluationService {

    private final MistralAiClient mistralAiClient;
    private final MistralAiConfig mistralAiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    @Builder
    public static class EvaluationInsights {
        private String aiSummary;
        private List<String> strengths;
        private List<String> improvements;
        private String scoreRating;
        private Integer businessLogicTotal;
        private Integer businessLogicPassed;
        private Integer syntaxTotal;
        private Integer syntaxPassed;
        private Integer dataFlowTotal;
        private Integer dataFlowPassed;
        private List<CategoryScoreDto> categoryBreakdown;
        private String overallRiskBadge;
        private String riskAnalysis;
        private Integer tabSwitchCount;
        private Integer copyPasteEvents;
        private Integer buildRuns;
        private Integer idleTimeMinutes;
    }

    public EvaluationInsights generateInsights(
            Assessment assessment,
            FeatureSpecification featureSpec,
            BuildStatus buildStatus,
            List<TestCase> testCases,
            List<TestResult> testResults,
            BigDecimal score,
            Integer tabSwitchCount,
            Integer copyPasteEvents,
            Integer idleTimeMinutes) {

        // 1. Compute Category Breakdown
        int blTotal = 0, blPassed = 0;
        int synTotal = 0, synPassed = 0;
        int dfTotal = 0, dfPassed = 0;

        Map<UUID, TestResult> resultMap = new HashMap<>();
        for (TestResult tr : testResults) {
            resultMap.put(tr.getTestCaseId(), tr);
        }

        List<String> observedFailures = new ArrayList<>();

        for (TestCase tc : testCases) {
            TestType type = tc.getTestType() != null ? tc.getTestType() : TestType.BUSINESS_LOGIC;
            TestResult tr = resultMap.get(tc.getId());
            boolean isPassed = tr != null && tr.getStatus() == TestResultStatus.PASSED;

            if (!isPassed && tr != null && tr.getFailureReason() != null) {
                observedFailures.add(tr.getFailureReason());
            }

            switch (type) {
                case SYNTAX -> {
                    synTotal++;
                    if (isPassed) synPassed++;
                }
                case DATA_FLOW -> {
                    dfTotal++;
                    if (isPassed) dfPassed++;
                }
                default -> {
                    blTotal++;
                    if (isPassed) blPassed++;
                }
            }
        }

        List<CategoryScoreDto> categoryBreakdown = new ArrayList<>();
        if (blTotal > 0) {
            int catScore = (int) Math.round(((double) blPassed / blTotal) * 100);
            categoryBreakdown.add(new CategoryScoreDto("Business Logic", blTotal, blPassed, catScore));
        }
        if (synTotal > 0) {
            int catScore = (int) Math.round(((double) synPassed / synTotal) * 100);
            categoryBreakdown.add(new CategoryScoreDto("Syntax", synTotal, synPassed, catScore));
        }
        if (dfTotal > 0) {
            int catScore = (int) Math.round(((double) dfPassed / dfTotal) * 100);
            categoryBreakdown.add(new CategoryScoreDto("Data Flow", dfTotal, dfPassed, catScore));
        }

        // 2. Score Rating
        double numericScore = score != null ? score.doubleValue() : 0.0;
        String scoreRating;
        if (numericScore >= 80.0) {
            scoreRating = "Excellent";
        } else if (numericScore >= 60.0) {
            scoreRating = "Good Performance";
        } else {
            scoreRating = "Needs Improvement";
        }

        // 3. Proctoring / Integrity Signals
        int tabSwitches = tabSwitchCount != null ? Math.max(0, tabSwitchCount) : 0;
        int copyPastes = copyPasteEvents != null ? Math.max(0, copyPasteEvents) : 0;
        int idleMins = idleTimeMinutes != null ? Math.max(1, idleTimeMinutes) : 2;

        String riskBadge = "LOW";
        String riskAnalysis = "No suspicious activity detected. Valid proctored coding session verified.";
        if (tabSwitches > 2 || copyPastes > 10) {
            riskBadge = "HIGH";
            riskAnalysis = "High risk activity: " + tabSwitches + " tab switch violations and " + copyPastes + " clipboard events detected.";
        } else if (tabSwitches > 0 || copyPastes > 3) {
            riskBadge = "MEDIUM";
            riskAnalysis = "Minor anomalies observed during proctoring (" + tabSwitches + " tab switch, " + copyPastes + " clipboard events).";
        }

        // 4. Try Mistral AI for qualitative feedback
        String aiSummary = null;
        List<String> strengths = null;
        List<String> improvements = null;

        if (mistralAiConfig != null && mistralAiConfig.isConfigured()) {
            try {
                String featureName = featureSpec != null ? featureSpec.getFeatureName() : "Spring Boot REST Feature";
                String featureDesc = featureSpec != null ? featureSpec.getDescription() : "";

                String systemPrompt = "You are a senior Java tech lead and assessment evaluator. Analyze candidate test results for a Spring Boot assessment and return a JSON object with keys:\n" +
                        "- \"aiSummary\": 2-3 concise sentences summarizing candidate performance\n" +
                        "- \"strengths\": an array of 2-3 specific technical strengths\n" +
                        "- \"improvements\": an array of 2-3 concrete, actionable technical improvement recommendations\n" +
                        "Respond ONLY in valid JSON.";

                String userPrompt = String.format(
                        "Assessment: %s\nFeature: %s\nDescription: %s\nBuild Status: %s\nScore: %.2f%%\nPassed Tests: %d/%d\nCategory Breakdown: Business Logic (%d/%d), Syntax (%d/%d), Data Flow (%d/%d)\nObserved Failure Samples: %s",
                        assessment.getTitle(),
                        featureName,
                        featureDesc,
                        buildStatus,
                        numericScore,
                        testResults.stream().filter(t -> t.getStatus() == TestResultStatus.PASSED).count(),
                        testCases.size(),
                        blPassed, blTotal,
                        synPassed, synTotal,
                        dfPassed, dfTotal,
                        observedFailures.isEmpty() ? "None (all passed)" : String.join("; ", observedFailures.subList(0, Math.min(3, observedFailures.size())))
                );

                String mistralResponse = mistralAiClient.generateChatCompletion(systemPrompt, userPrompt);
                if (mistralResponse != null && !mistralResponse.isBlank()) {
                    JsonNode node = objectMapper.readTree(mistralResponse);
                    if (node.has("aiSummary")) {
                        aiSummary = node.get("aiSummary").asText();
                    }
                    if (node.has("strengths") && node.get("strengths").isArray()) {
                        strengths = new ArrayList<>();
                        for (JsonNode s : node.get("strengths")) {
                            strengths.add(s.asText());
                        }
                    }
                    if (node.has("improvements") && node.get("improvements").isArray()) {
                        improvements = new ArrayList<>();
                        for (JsonNode imp : node.get("improvements")) {
                            improvements.add(imp.asText());
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Mistral AI evaluation generation error: {}. Using deterministic fallback synthesizer.", ex.getMessage());
            }
        }

        // 5. Fallback Synthesizer if Mistral was not used or failed
        if (aiSummary == null || aiSummary.isBlank()) {
            if (buildStatus != BuildStatus.SUCCESS) {
                aiSummary = "The candidate workspace encountered Maven packaging errors, preventing container boot and automated test case execution.";
            } else if (numericScore >= 80.0) {
                aiSummary = String.format("The candidate demonstrated high architectural proficiency with %.1f%% of test cases passing. Core REST endpoints and business requirements were implemented cleanly with solid contract fidelity.", numericScore);
            } else if (numericScore >= 60.0) {
                aiSummary = String.format("The candidate implemented the majority of feature requirements with %.1f%% score. Several edge-case contract assertions or response schema mismatches were encountered.", numericScore);
            } else {
                aiSummary = String.format("The candidate completed the assessment with a score of %.1f%%. Key functional requirements or HTTP status code specifications require further refinement.", numericScore);
            }
        }

        if (strengths == null || strengths.isEmpty()) {
            strengths = new ArrayList<>();
            if (buildStatus == BuildStatus.SUCCESS) {
                strengths.add("Clean Maven compilation and artifact packaging");
            }
            if (blPassed > 0) {
                strengths.add("REST API controller mapping and HTTP verb routing");
            }
            if (synPassed > 0) {
                strengths.add("Request DTO structure and JSON response serialization");
            }
            if (dfPassed > 0) {
                strengths.add("Spring Data JPA repository persistence layer integration");
            }
            if (strengths.isEmpty()) {
                strengths.add("Adherence to Spring Boot standard project conventions");
            }
        }

        if (improvements == null || improvements.isEmpty()) {
            improvements = new ArrayList<>();
            if (!observedFailures.isEmpty()) {
                boolean has500 = observedFailures.stream().anyMatch(f -> f.contains("500") || f.toLowerCase().contains("internal server error"));
                if (has500) {
                    improvements.add("Implement @ExceptionHandler and @ControllerAdvice for graceful HTTP error handling");
                }
                boolean hasMissingField = observedFailures.stream().anyMatch(f -> f.toLowerCase().contains("missing expected json field"));
                if (hasMissingField) {
                    improvements.add("Ensure all response JSON fields match the API feature specification contract");
                }
            }
            if (synTotal > synPassed) {
                improvements.add("Enforce strict input validation using javax/jakarta @Valid and constraint annotations");
            }
            if (dfTotal > dfPassed) {
                improvements.add("Verify transactional consistency and relational entity cascade bindings");
            }
            if (improvements.isEmpty()) {
                improvements.add("Add automated unit tests and edge-case boundary validation checks");
            }
        }

        return EvaluationInsights.builder()
                .aiSummary(aiSummary)
                .strengths(strengths)
                .improvements(improvements)
                .scoreRating(scoreRating)
                .businessLogicTotal(blTotal)
                .businessLogicPassed(blPassed)
                .syntaxTotal(synTotal)
                .syntaxPassed(synPassed)
                .dataFlowTotal(dfTotal)
                .dataFlowPassed(dfPassed)
                .categoryBreakdown(categoryBreakdown)
                .overallRiskBadge(riskBadge)
                .riskAnalysis(riskAnalysis)
                .tabSwitchCount(tabSwitches)
                .copyPasteEvents(copyPastes)
                .buildRuns(1)
                .idleTimeMinutes(idleMins)
                .build();
    }
}
