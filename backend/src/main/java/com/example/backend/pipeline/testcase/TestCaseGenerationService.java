package com.example.backend.pipeline.testcase;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.TestCaseRepository;
import com.example.backend.common.enums.TestType;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TestCaseGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TestCaseGenerationService.class);

    private final MistralAiClient mistralAiClient;
    private final MistralAiConfig mistralAiConfig;
    private final TestCasePromptBuilder promptBuilder;
    private final TestCaseRepository testCaseRepository;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public TestCaseGenerationService(MistralAiClient mistralAiClient,
                                     MistralAiConfig mistralAiConfig,
                                     TestCasePromptBuilder promptBuilder,
                                     TestCaseRepository testCaseRepository,
                                     AssessmentRepository assessmentRepository) {
        this.mistralAiClient = mistralAiClient;
        this.mistralAiConfig = mistralAiConfig;
        this.promptBuilder = promptBuilder;
        this.testCaseRepository = testCaseRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes Phase 5: Generates Black-Box Test Cases using Mistral AI (or dynamic domain test generator).
     */
    @Transactional
    public TestCaseGenerationResult generateTestCases(UUID assessmentId, FeatureGenerationResult featureResult) {
        log.info("Phase 5: Starting AI Black-Box Test Case Generation for Assessment {}", assessmentId);

        List<TestCaseItemDto> testCases = new ArrayList<>();
        String rawOutput = null;
        boolean llmSucceeded = false;

        // 1. Attempt LLM Generation via Mistral AI
        if (mistralAiConfig != null && mistralAiConfig.isConfigured()) {
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt();
                String userPrompt = promptBuilder.buildUserPrompt(featureResult);

                log.info("Invoking Mistral AI ({}) with structured feature prompt (length: {} chars)...",
                        mistralAiConfig.getModel(), userPrompt.length());

                rawOutput = mistralAiClient.generateChatCompletion(systemPrompt, userPrompt);
                log.info("Received Mistral AI test cases response (Length: {} chars)", rawOutput.length());

                String cleanJson = extractJson(rawOutput);
                JsonNode root = objectMapper.readTree(cleanJson);
                JsonNode arrayNode = root.path("testCases");

                if (arrayNode.isArray() && !arrayNode.isEmpty()) {
                    for (int i = 0; i < arrayNode.size(); i++) {
                        JsonNode node = arrayNode.get(i);
                        int num = node.path("testCaseNumber").asInt(i + 1);
                        String tTypeStr = node.path("testType").asText("BUSINESS_LOGIC");
                        TestType testType = parseTestType(tTypeStr);
                        String method = node.path("httpMethod").asText("GET");
                        String endpoint = node.path("endpoint").asText("/");
                        String reqData = node.has("requestData") ? nodeToFormattedString(node.path("requestData")) : null;
                        int status = node.path("expectedStatusCode").asInt(200);
                        String respData = node.has("expectedResponse") ? nodeToFormattedString(node.path("expectedResponse")) : null;
                        String assertions = node.has("assertions") ? nodeToFormattedString(node.path("assertions")) : "[]";
                        BigDecimal weight = BigDecimal.valueOf(node.path("weight").asDouble(1.00));
                        String desc = node.path("description").asText("");

                        testCases.add(new TestCaseItemDto(num, testType, method, endpoint, reqData, status, respData, assertions, weight, desc));
                    }
                    if (!testCases.isEmpty()) {
                        llmSucceeded = true;
                        log.info("Successfully parsed {} Mistral AI generated Black-Box test cases", testCases.size());
                    }
                }
            } catch (Exception ex) {
                log.warn("Mistral AI test case generation was not completed: {}. Falling back to dynamic test generator.", ex.getMessage());
            }
        } else {
            log.info("Mistral AI API is in offline mode. Utilizing dynamic test case generator.");
        }

        // 2. Dynamic Domain-Aligned Generator (Fallback / Offline / Deterministic mode)
        if (!llmSucceeded) {
            testCases = generateDynamicTestSuite(assessmentId, featureResult);
            rawOutput = "dynamic-ast-test-generator-v2";
        }

        // 3. Persist to PostgreSQL Database
        persistTestCases(assessmentId, testCases);

        log.info("Phase 5: Test Case Generation COMPLETED for Assessment {} (Generated {} test cases)", assessmentId, testCases.size());

        return TestCaseGenerationResult.ok(assessmentId, testCases, rawOutput);
    }

    private String nodeToFormattedString(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText().trim();
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return node.toString();
        }
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf("\n");
            int lastBackticks = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastBackticks > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastBackticks).trim();
            }
        }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private void persistTestCases(UUID assessmentId, List<TestCaseItemDto> dtoList) {
        if (testCaseRepository == null || assessmentId == null) {
            return;
        }

        try {
            Assessment assessment = assessmentRepository != null ? assessmentRepository.findById(assessmentId).orElse(null) : null;
            if (assessment == null) {
                return;
            }

            testCaseRepository.deleteByAssessmentId(assessmentId);

            List<TestCase> entities = new ArrayList<>();
            for (TestCaseItemDto dto : dtoList) {
                TestCase entity = new TestCase(
                        assessment,
                        dto.getTestCaseNumber(),
                        dto.getTestType(),
                        dto.getHttpMethod(),
                        dto.getEndpoint(),
                        dto.getRequestData(),
                        dto.getExpectedStatusCode(),
                        dto.getExpectedResponse(),
                        dto.getAssertions(),
                        dto.getWeight()
                );
                entities.add(entity);
            }

            testCaseRepository.saveAll(entities);
            log.info("Phase 5: Saved {} TEST_CASE records to DB for assessment {}", entities.size(), assessmentId);
        } catch (Exception ex) {
            log.warn("Could not persist TEST_CASE entities to DB: {}", ex.getMessage());
        }
    }

    private TestType parseTestType(String val) {
        if (val == null) return TestType.BUSINESS_LOGIC;
        try {
            return TestType.valueOf(val.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return TestType.BUSINESS_LOGIC;
        }
    }

    /**
     * Synthesizes dynamic test cases based on the generated feature endpoints and payloads.
     */
    public List<TestCaseItemDto> generateDynamicTestSuite(UUID assessmentId, FeatureGenerationResult featureResult) {
        List<TestCaseItemDto> suite = new ArrayList<>();

        String featureName = featureResult != null && featureResult.getFeatureName() != null ? featureResult.getFeatureName() : "Feature";
        String reqSpec = featureResult != null && featureResult.getRequestSpecification() != null ? featureResult.getRequestSpecification() : "";

        // Extract primary endpoint from request specification (e.g. POST /orders, POST /enrollments, etc.)
        String primaryEndpoint = "/items";
        String sampleBody = "{\"name\": \"Test Item\", \"status\": \"ACTIVE\"}";

        for (String line : reqSpec.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("POST /") || trimmed.startsWith("PUT /") || trimmed.startsWith("GET /")) {
                String[] parts = trimmed.split(" ");
                if (parts.length >= 2) {
                    primaryEndpoint = parts[1].split("\\?")[0].trim();
                    break;
                }
            }
        }

        // Test 1: Happy Path Creation
        suite.add(new TestCaseItemDto(
                1,
                TestType.BUSINESS_LOGIC,
                "POST",
                primaryEndpoint,
                sampleBody,
                201,
                "{\"id\": 1, \"status\": \"ACTIVE\"}",
                "[\"response.statusCode == 201\", \"response.body.id != null\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Successfully create and register new " + featureName + " resource"
        ));

        // Test 2: Happy Path Retrieval
        String getEndpoint = primaryEndpoint.contains("{") ? primaryEndpoint.replaceAll("\\{[^}]+\\}", "1") : primaryEndpoint + "/1";
        suite.add(new TestCaseItemDto(
                2,
                TestType.INTEGRATION,
                "GET",
                getEndpoint,
                null,
                200,
                "{\"id\": 1}",
                "[\"response.statusCode == 200\", \"response.body.id != null\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Retrieve resource details by ID"
        ));

        // Test 3: Status Transition / Update
        suite.add(new TestCaseItemDto(
                3,
                TestType.BUSINESS_LOGIC,
                "PUT",
                getEndpoint + "/status",
                "{\"status\": \"COMPLETED\"}",
                200,
                "{\"id\": 1, \"status\": \"COMPLETED\"}",
                "[\"response.statusCode == 200\", \"response.body.status == 'COMPLETED'\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Transition status to COMPLETED"
        ));

        // Test 4: Validation Edge Case
        suite.add(new TestCaseItemDto(
                4,
                TestType.SYNTAX,
                "POST",
                primaryEndpoint,
                "{}",
                400,
                "{\"error\": \"Validation failed\"}",
                "[\"response.statusCode == 400\"]",
                BigDecimal.valueOf(1.00),
                "Validation Edge Case: Reject empty or invalid request body with 400 Bad Request"
        ));

        // Test 5: Not Found Handling
        suite.add(new TestCaseItemDto(
                5,
                TestType.DATA_FLOW,
                "GET",
                primaryEndpoint.replaceAll("/$", "") + "/999999",
                null,
                404,
                "{\"error\": \"Resource not found\"}",
                "[\"response.statusCode == 404\"]",
                BigDecimal.valueOf(1.00),
                "Error Handling: Return 404 Not Found for non-existent resource identifier"
        ));

        return suite;
    }
}
