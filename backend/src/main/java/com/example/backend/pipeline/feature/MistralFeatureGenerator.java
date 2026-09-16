package com.example.backend.pipeline.feature;

import com.example.backend.common.enums.Difficulty;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class MistralFeatureGenerator implements FeatureGenerator {

    private static final Logger log = LoggerFactory.getLogger(MistralFeatureGenerator.class);

    private final MistralAiClient mistralAiClient;
    private final MistralAiConfig mistralAiConfig;
    private final FeaturePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public MistralFeatureGenerator(MistralAiClient mistralAiClient,
                                   MistralAiConfig mistralAiConfig,
                                   FeaturePromptBuilder promptBuilder) {
        this.mistralAiClient = mistralAiClient;
        this.mistralAiConfig = mistralAiConfig;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult, Difficulty difficulty) {
        Difficulty effectiveDifficulty = difficulty != null ? difficulty : Difficulty.INTERMEDIATE;

        if (mistralAiConfig == null || !mistralAiConfig.isConfigured()) {
            log.warn("assessmentId={} Mistral AI is not configured — skipping AI generation", assessmentId);
            return null;
        }

        try {
            String systemPrompt = promptBuilder.buildSystemPrompt(effectiveDifficulty);
            String userPrompt = promptBuilder.buildUserPrompt(astResult, effectiveDifficulty);

            log.info("assessmentId={} difficulty={} Invoking Mistral AI ({}) with structured AST prompt (length: {} chars)...",
                    assessmentId, effectiveDifficulty, mistralAiConfig.getModel(), userPrompt.length());

            String rawOutput = mistralAiClient.generateChatCompletion(systemPrompt, userPrompt);
            log.info("assessmentId={} Received Mistral AI feature response (Length: {} chars)", assessmentId, rawOutput.length());

            String cleanJson = extractJson(rawOutput);
            JsonNode root = objectMapper.readTree(cleanJson);

            String featureName  = root.path("featureName").asText("").trim();
            String description  = root.path("description").asText("").trim();
            String requirements = formatSection(root.path("requirements"));
            String constraints  = formatSection(root.path("constraints"));

            // New structured fields: requestBody and responseBody (objects → compact JSON strings)
            String requestSpec  = nodeToCompactJson(root.path("requestBody"));
            String responseSpec = nodeToCompactJson(root.path("responseBody"));

            // Fallback: also accept legacy free-text requestSpecification / responseSpecification
            if (requestSpec.equals("{}") || requestSpec.isEmpty()) {
                requestSpec = formatSection(root.path("requestSpecification"));
            }
            if (responseSpec.equals("{}") || responseSpec.isEmpty()) {
                responseSpec = formatSection(root.path("responseSpecification"));
            }

            String endpoint   = extractEndpoint(root, requestSpec, astResult);
            String httpMethod = extractHttpMethod(root, requestSpec);

            // Test case seed — JSON array string
            String testCaseSeed = nodeToCompactJson(root.path("testCases"));
            if ("{}".equals(testCaseSeed) || testCaseSeed.isEmpty()) {
                testCaseSeed = null;
            }

            if (!featureName.isEmpty() && (!description.isEmpty() || !requirements.isEmpty())) {
                FeatureGenerationResult result = FeatureGenerationResult.ok(
                        assessmentId,
                        featureName,
                        description,
                        requirements,
                        requestSpec,
                        responseSpec,
                        constraints,
                        rawOutput,
                        endpoint,
                        httpMethod
                );
                result.setTestCaseSeed(testCaseSeed);
                log.info("assessmentId={} difficulty={} Mistral AI feature generation succeeded. Feature: '{}', Endpoint: [{} {}], TestCases: {}",
                        assessmentId, effectiveDifficulty, featureName, httpMethod, endpoint,
                        testCaseSeed != null ? "present" : "absent");
                return result;
            }

            log.warn("assessmentId={} Mistral AI returned an empty featureName or description — falling back", assessmentId);
        } catch (Exception ex) {
            log.warn("assessmentId={} Mistral AI generation failed: {}", assessmentId, ex.getMessage());
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Converts a JsonNode to a compact JSON string.
     * Returns "{}" for missing/null nodes, and the raw array string for arrays.
     */
    private String nodeToCompactJson(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return node.toString();
        }
    }

    private String extractEndpoint(JsonNode root, String requestSpec, AstAnalysisResult astResult) {
        if (root != null && root.has("endpoint") && !root.path("endpoint").asText().trim().isEmpty()) {
            return root.path("endpoint").asText().trim();
        }
        if (requestSpec != null && !requestSpec.startsWith("{")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(POST|GET|PUT|DELETE|PATCH)\\s+([/a-zA-Z0-9_{}-]+)").matcher(requestSpec);
            if (matcher.find()) {
                return matcher.group(2).trim();
            }
        }
        if (astResult != null && astResult.getSourceCodeStructure() != null
                && astResult.getSourceCodeStructure().getEntities() != null
                && !astResult.getSourceCodeStructure().getEntities().isEmpty()) {
            return "/" + pluralize(astResult.getSourceCodeStructure().getEntities().get(0).getClassName());
        }
        return "/api/v1/resource";
    }

    private String extractHttpMethod(JsonNode root, String requestSpec) {
        if (root != null && root.has("httpMethod") && !root.path("httpMethod").asText().trim().isEmpty()) {
            return root.path("httpMethod").asText().trim().toUpperCase(Locale.ROOT);
        }
        if (requestSpec != null && !requestSpec.startsWith("{")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(POST|GET|PUT|DELETE|PATCH)\\s+([/a-zA-Z0-9_{}-]+)").matcher(requestSpec);
            if (matcher.find()) {
                return matcher.group(1).trim().toUpperCase(Locale.ROOT);
            }
        }
        return "POST";
    }

    private String formatSection(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText().trim();
        }
        if (node.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < node.size(); i++) {
                JsonNode item = node.get(i);
                if (item.isTextual()) {
                    String text = item.asText().trim();
                    sb.append(text.matches("^\\d+\\..*") ? text : (i + 1) + ". " + text).append("\n");
                } else {
                    try {
                        sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item)).append("\n");
                    } catch (Exception e) {
                        sb.append(item.toString()).append("\n");
                    }
                }
            }
            return sb.toString().trim();
        }
        if (node.isObject()) {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            } catch (Exception e) {
                return node.toString();
            }
        }
        return node.asText();
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastBackticks = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastBackticks > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastBackticks).trim();
            }
        }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace  = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private String pluralize(String name) {
        if (name == null || name.isEmpty()) return "items";
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("s")) return lower;
        if (lower.endsWith("y")) return lower.substring(0, lower.length() - 1) + "ies";
        return lower + "s";
    }
}
