package com.example.backend.pipeline.testcase;

import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import java.util.UUID;

public interface TestGenerator {
    TestCaseGenerationResult generateTestCases(UUID assessmentId, FeatureGenerationResult featureResult);
}
