package com.example.backend.pipeline.testcase;

import com.example.backend.pipeline.analysis.AstAnalysisService;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.FeaturePromptBuilder;
import com.example.backend.pipeline.feature.FeatureSpecificationService;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestCaseGenerationServiceTest {

    @Test
    @DisplayName("Phase 5: Generate Black-Box Test Cases for real Student-Management-System repository")
    void testTestCaseGenerationOnRealRepo() {
        AstAnalysisService astService = new AstAnalysisService(null, null);

        MistralAiConfig config = new MistralAiConfig();
        config.setApiKey("your_mistral_api_key_here");
        MistralAiClient client = new MistralAiClient(config);

        FeaturePromptBuilder featurePromptBuilder = new FeaturePromptBuilder();
        FeatureSpecificationService featureService = new FeatureSpecificationService(
                client, config, featurePromptBuilder, null, null
        );

        TestCasePromptBuilder testCasePromptBuilder = new TestCasePromptBuilder();
        TestCaseGenerationService testCaseService = new TestCaseGenerationService(
                client, config, testCasePromptBuilder, null, null
        );

        UUID assessmentId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Path repoRoot = Paths.get(System.getProperty("user.dir"), "storage", "assessments", assessmentId.toString(), "original");
        String subDirectory = "github_practice";

        System.out.println("==================================================");
        System.out.println("STARTING PHASE 5: AI BLACK-BOX TEST CASE GENERATION");
        System.out.println("Assessment ID:   " + assessmentId);
        System.out.println("Repository Root: " + repoRoot.toAbsolutePath());
        System.out.println("==================================================");

        // 1. Run Phase 3
        AstAnalysisResult astResult = astService.analyzeSourceCode(assessmentId, repoRoot, subDirectory);
        assertTrue(astResult.isSuccess(), "Phase 3 AST analysis must succeed");

        // 2. Run Phase 4
        FeatureGenerationResult featureResult = featureService.generateFeatureSpecification(assessmentId, astResult);
        assertTrue(featureResult.isSuccess(), "Phase 4 Feature specification must succeed");

        // 3. Run Phase 5
        TestCaseGenerationResult result = testCaseService.generateTestCases(assessmentId, featureResult);

        System.out.println("==================================================");
        System.out.println("PHASE 5 EXECUTION OUTCOME:");
        System.out.println("Status Success:     " + result.isSuccess());
        System.out.println("Total Test Cases:   " + result.getTotalTestCases());
        System.out.println("--------------------------------------------------");

        for (TestCaseItemDto tc : result.getTestCases()) {
            System.out.println("TEST CASE #" + tc.getTestCaseNumber() + " [" + tc.getTestType() + " | Weight: " + tc.getWeight() + "]");
            System.out.println("  Description:     " + tc.getDescription());
            System.out.println("  HTTP Call:       " + tc.getHttpMethod() + " " + tc.getEndpoint());
            if (tc.getRequestData() != null) {
                System.out.println("  Request Body:    " + tc.getRequestData());
            }
            System.out.println("  Expected Status: " + tc.getExpectedStatusCode());
            if (tc.getExpectedResponse() != null) {
                System.out.println("  Expected Resp:   " + tc.getExpectedResponse());
            }
            System.out.println("  Assertions:      " + tc.getAssertions());
            System.out.println("--------------------------------------------------");
        }

        System.out.println("==================================================");

        assertTrue(result.isSuccess(), "Test case generation must succeed");
        assertTrue(result.getTotalTestCases() >= 5, "Must generate at least 5 test cases");
    }
}
