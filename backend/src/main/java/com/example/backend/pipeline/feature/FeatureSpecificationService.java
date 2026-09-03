package com.example.backend.pipeline.feature;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.FeatureSpecificationRepository;
import com.example.backend.pipeline.analysis.dto.*;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Executes Phase 4: Generates AI Feature Specification using Mistral AI (or dynamic AST domain synthesizer).
     */
    @Transactional
    public FeatureGenerationResult generateFeatureSpecification(UUID assessmentId, AstAnalysisResult astResult) {
        log.info("Phase 4: Starting AI Feature Specification Generation for Assessment {}", assessmentId);

        FeatureGenerationResult result = mistralFeatureGenerator.generateFeature(assessmentId, astResult);
        
        if (result == null) {
            log.info("Mistral AI generation returned null. Falling back to dynamic AST domain synthesizer.");
            result = dynamicFeatureGenerator.generateFeature(assessmentId, astResult);
        }

        // Persist to PostgreSQL Database
        persistFeatureSpecification(
                result.getAssessmentId(),
                result.getFeatureName(),
                result.getDescription(),
                result.getRequirements(),
                result.getRequestSpecification(),
                result.getResponseSpecification(),
                result.getConstraints(),
                result.getEndpoint(),
                result.getHttpMethod()
        );

        log.info("Phase 4: Feature Specification Generation COMPLETED for Assessment {} (Feature: {})", assessmentId, result.getFeatureName());

        return result;
    }

    private void persistFeatureSpecification(UUID assessmentId, String name, String desc, String req,
                                             String reqSpec, String respSpec, String constr,
                                             String endpoint, String httpMethod) {
        if (featureSpecificationRepository == null || assessmentId == null) {
            return;
        }

        try {
            FeatureSpecification spec = featureSpecificationRepository.findById(assessmentId).orElse(null);
            if (spec == null && assessmentRepository != null) {
                Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
                if (assessment != null) {
                    spec = new FeatureSpecification(assessment, name, desc, req, reqSpec, respSpec, constr, endpoint, httpMethod);
                }
            }

            if (spec != null) {
                spec.setFeatureName(name);
                spec.setDescription(desc);
                spec.setRequirements(req);
                spec.setRequestSpecification(reqSpec);
                spec.setResponseSpecification(respSpec);
                spec.setConstraints(constr);
                spec.setEndpoint(endpoint != null && !endpoint.isBlank() ? endpoint : "/api/v1/resource");
                spec.setHttpMethod(httpMethod != null && !httpMethod.isBlank() ? httpMethod : "POST");
                featureSpecificationRepository.save(spec);
                log.info("Phase 4: Saved FEATURE_SPECIFICATION record for assessment {} (Method: {}, Endpoint: {})",
                        assessmentId, spec.getHttpMethod(), spec.getEndpoint());
            }
        } catch (Exception ex) {
            log.warn("Could not persist FEATURE_SPECIFICATION to DB: {}", ex.getMessage());
        }
    }
}
