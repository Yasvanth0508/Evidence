package com.example.backend.pipeline.feature;

import com.example.backend.pipeline.analysis.AstAnalysisService;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureSpecificationServiceTest {

    @Test
    @DisplayName("Phase 4: Generate AI Feature Specification for real Student-Management-System repository")
    void testFeatureGenerationOnRealRepo() {
        AstAnalysisService astService = new AstAnalysisService(null, null);

        MistralAiConfig config = new MistralAiConfig();
        config.setApiKey("your_mistral_api_key_here"); // Placeholder
        MistralAiClient client = new MistralAiClient(config);
        FeaturePromptBuilder promptBuilder = new FeaturePromptBuilder();

        FeatureSpecificationService featureService = new FeatureSpecificationService(
                client, config, promptBuilder, null, null
        );

        UUID assessmentId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Path repoRoot = Paths.get(System.getProperty("user.dir"), "storage", "assessments", assessmentId.toString(), "original");
        String subDirectory = "github_practice";

        System.out.println("==================================================");
        System.out.println("STARTING PHASE 4: AI FEATURE SPECIFICATION GENERATION");
        System.out.println("Assessment ID:   " + assessmentId);
        System.out.println("Repository Root: " + repoRoot.toAbsolutePath());
        System.out.println("==================================================");

        // 1. Run Phase 3 AST Extraction
        AstAnalysisResult astResult = astService.analyzeSourceCode(assessmentId, repoRoot, subDirectory);
        assertTrue(astResult.isSuccess(), "Phase 3 AST analysis must succeed");

        // 2. Run Phase 4 Feature Generation
        FeatureGenerationResult result = featureService.generateFeatureSpecification(assessmentId, astResult);

        System.out.println("==================================================");
        System.out.println("PHASE 4 EXECUTION OUTCOME:");
        System.out.println("Status Success:       " + result.isSuccess());
        System.out.println("Feature Title:        " + result.getFeatureName());
        System.out.println("--------------------------------------------------");
        System.out.println("FEATURE DESCRIPTION:");
        System.out.println(result.getDescription());
        System.out.println("--------------------------------------------------");
        System.out.println("TECHNICAL REQUIREMENTS:");
        System.out.println(result.getRequirements());
        System.out.println("--------------------------------------------------");
        System.out.println("REQUEST SPECIFICATION:");
        System.out.println(result.getRequestSpecification());
        System.out.println("--------------------------------------------------");
        System.out.println("RESPONSE SPECIFICATION:");
        System.out.println(result.getResponseSpecification());
        System.out.println("--------------------------------------------------");
        System.out.println("CONSTRAINTS:");
        System.out.println(result.getConstraints());
        System.out.println("==================================================");

        assertTrue(result.isSuccess(), "Feature generation must succeed");
        assertNotNull(result.getFeatureName(), "Feature name must not be null");
        assertNotNull(result.getDescription(), "Description must not be null");
        assertNotNull(result.getRequirements(), "Requirements must not be null");
    }
}
