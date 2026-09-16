package com.example.backend.pipeline.feature;

import com.example.backend.common.enums.Difficulty;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import java.util.UUID;

public interface FeatureGenerator {

    /**
     * Generates a feature specification for the given assessment, using the AST and difficulty level.
     */
    FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult, Difficulty difficulty);

    /**
     * Backward-compatible overload — defaults to INTERMEDIATE difficulty.
     */
    default FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult) {
        return generateFeature(assessmentId, astResult, Difficulty.INTERMEDIATE);
    }
}
