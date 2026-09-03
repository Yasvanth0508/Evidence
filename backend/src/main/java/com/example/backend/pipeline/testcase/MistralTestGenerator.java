package com.example.backend.pipeline.testcase;

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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MistralTestGenerator implements TestGenerator {

    private static final Logger log = LoggerFactory.getLogger(MistralTestGenerator.class);

    private final MistralAiClient mistralAiClient;
    private final MistralAiConfig mistralAiConfig;
    private final TestCasePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public MistralTestGenerator(MistralAiClient mistralAiClient,
                                MistralAiConfig mistralAiConfig,
                                TestCasePromptBuilder promptBuilder) {
        this.mistralAiClient = mistralAiClient;
        this.mistralAiConfig = mistralAiConfig;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public TestCaseGenerationResult generateTestCases(UUID assessmentId, FeatureGenerationResult featureResult) {
        if (mistralAiConfig == null || !mistralAiConfig.isConfigured()) {
            return null;
        }

        List<TestCaseItemDto> testCases = new ArrayList<>();
        String rawOutput = null;

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
                    log.info("Successfully parsed {} Mistral AI generated Black-Box test cases", testCases.size());
                    return TestCaseGenerationResult.ok(assessmentId, testCases, rawOutput);
                }
            }
        } catch (Exception ex) {
            log.warn("Mistral AI test case generation was not completed: {}", ex.getMessage());
        }

        return null;
    }

    private TestType parseTestType(String val) {
        if (val == null) return TestType.BUSINESS_LOGIC;
        try {
            return TestType.valueOf(val.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return TestType.BUSINESS_LOGIC;
        }
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
            int firstNewline = trimmed.indexOf("\\n");
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
}
