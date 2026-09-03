package com.example.backend.pipeline.feature;

import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import java.util.UUID;

public interface FeatureGenerator {
    FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult);
}
