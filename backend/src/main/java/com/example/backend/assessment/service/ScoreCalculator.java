package com.example.backend.assessment.service;

import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.entity.TestResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreCalculator {

    @Getter
    @Builder
    public static class ScoreResult {
        private BigDecimal totalWeight;
        private BigDecimal passedWeight;
        private int totalTests;
        private int passedTests;
        private int failedTests;
        private BigDecimal finalScore;
    }

    public ScoreResult calculate(List<TestCase> testCases, List<TestResult> testResults) {
        Map<java.util.UUID, TestResult> resultMap = testResults.stream()
                .collect(Collectors.toMap(r -> r.getTestCase().getId(), r -> r));

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal passedWeight = BigDecimal.ZERO;
        int passedTests = 0;
        int failedTests = 0;

        for (TestCase tc : testCases) {
            BigDecimal weight = tc.getWeight() != null ? tc.getWeight() : BigDecimal.ONE;
            totalWeight = totalWeight.add(weight);

            TestResult result = resultMap.get(tc.getId());
            if (result != null && result.getStatus() == com.example.backend.common.enums.TestResultStatus.PASSED) {
                passedWeight = passedWeight.add(weight);
                passedTests++;
            } else {
                failedTests++;
            }
        }

        BigDecimal finalScore = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalScore = passedWeight.divide(totalWeight, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ScoreResult.builder()
                .totalWeight(totalWeight)
                .passedWeight(passedWeight)
                .totalTests(testCases.size())
                .passedTests(passedTests)
                .failedTests(failedTests)
                .finalScore(finalScore)
                .build();
    }
}
