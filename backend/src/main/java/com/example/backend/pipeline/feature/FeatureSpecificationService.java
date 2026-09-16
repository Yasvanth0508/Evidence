package com.example.backend.pipeline.feature;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.FeatureSpecificationRepository;
import com.example.backend.common.enums.Difficulty;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FeatureSpecificationService {

    private static final Logger log = LoggerFactory.getLogger(FeatureSpecificationService.class);

    private final FeatureGenerator mistralFeatureGenerator;
    private final FeatureGenerator dynamicFeatureGenerator;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final AssessmentRepository assessmentRepository;

    public FeatureSpecificationService(MistralFeatureGenerator mistralFeatureGenerator,
                                       DynamicFeatureGenerator dynamicFeatureGenerator,
                                       FeatureSpecificationRepository featureSpecificationRepository,
                                       AssessmentRepository assessmentRepository) {
        this.mistralFeatureGenerator = mistralFeatureGenerator;
        this.dynamicFeatureGenerator = dynamicFeatureGenerator;
        this.featureSpecificationRepository = featureSpecificationRepository;
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Executes Phase 4: Generates AI Feature Specification using Mistral AI
     * (with difficulty-calibrated prompt) or falls back to the dynamic AST synthesizer.
     */
    @Transactional
    public FeatureGenerationResult generateFeatureSpecification(UUID assessmentId,
                                                                AstAnalysisResult astResult,
                                                                Difficulty difficulty) {
        Difficulty d = difficulty != null ? difficulty : Difficulty.INTERMEDIATE;
        log.info("Phase 4: Starting Feature Specification Generation for Assessment {} (difficulty={})",
                assessmentId, d);

        FeatureGenerationResult result = mistralFeatureGenerator.generateFeature(assessmentId, astResult, d);

        if (result == null) {
            log.info("assessmentId={} Mistral AI generation returned null — falling back to DynamicFeatureGenerator (difficulty={})",
                    assessmentId, d);
            result = dynamicFeatureGenerator.generateFeature(assessmentId, astResult, d);
        }

        // Persist to PostgreSQL
        persistFeatureSpecification(result);

        log.info("Phase 4: Feature Specification Generation COMPLETED for Assessment {} " +
                 "(Feature: '{}', Endpoint: [{} {}], TestCaseSeed: {})",
                assessmentId, result.getFeatureName(), result.getHttpMethod(), result.getEndpoint(),
                result.getTestCaseSeed() != null ? "present" : "absent");

        return result;
    }

    /**
     * Backward-compatible overload — reads difficulty from the Assessment entity.
     */
    @Transactional
    public FeatureGenerationResult generateFeatureSpecification(UUID assessmentId, AstAnalysisResult astResult) {
        Difficulty difficulty = Difficulty.INTERMEDIATE;
        if (assessmentRepository != null) {
            Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
            if (assessment != null && assessment.getDifficulty() != null) {
                difficulty = assessment.getDifficulty();
            }
        }
        return generateFeatureSpecification(assessmentId, astResult, difficulty);
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    private void persistFeatureSpecification(FeatureGenerationResult result) {
        if (featureSpecificationRepository == null || result == null || result.getAssessmentId() == null) {
            return;
        }
        UUID assessmentId = result.getAssessmentId();
        try {
            FeatureSpecification spec = featureSpecificationRepository.findById(assessmentId).orElse(null);
            if (spec == null && assessmentRepository != null) {
                Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
                if (assessment != null) {
                    spec = new FeatureSpecification(assessment,
                            result.getFeatureName(),
                            result.getDescription(),
                            result.getRequirements(),
                            result.getRequestSpecification(),
                            result.getResponseSpecification(),
                            result.getConstraints(),
                            result.getEndpoint(),
                            result.getHttpMethod(),
                            result.getTestCaseSeed());
                }
            }

            if (spec != null) {
                spec.setFeatureName(result.getFeatureName());
                spec.setDescription(result.getDescription());
                spec.setRequirements(result.getRequirements());
                spec.setRequestSpecification(result.getRequestSpecification());
                spec.setResponseSpecification(result.getResponseSpecification());
                spec.setConstraints(result.getConstraints());
                spec.setEndpoint(result.getEndpoint() != null && !result.getEndpoint().isBlank()
                        ? result.getEndpoint() : "/api/v1/resource");
                spec.setHttpMethod(result.getHttpMethod() != null && !result.getHttpMethod().isBlank()
                        ? result.getHttpMethod() : "POST");
                spec.setTestCaseSeed(result.getTestCaseSeed());
                featureSpecificationRepository.save(spec);
                log.info("Phase 4: Saved FEATURE_SPECIFICATION for assessment {} (Method: {}, Endpoint: {})",
                        assessmentId, spec.getHttpMethod(), spec.getEndpoint());
            }
        } catch (Exception ex) {
            log.warn("Could not persist FEATURE_SPECIFICATION to DB: {}", ex.getMessage());
        }
    }
}
