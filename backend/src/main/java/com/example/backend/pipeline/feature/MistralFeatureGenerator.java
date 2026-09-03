package com.example.backend.pipeline.feature;

import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Locale;

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
    public FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult) {
        if (mistralAiConfig == null || !mistralAiConfig.isConfigured()) {
            return null; // Signals failure/not configured, fallback will handle
        }

        try {
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(astResult);

            log.info("Invoking Mistral AI ({}) with structured AST codebase prompt (length: {} chars)...",
                    mistralAiConfig.getModel(), userPrompt.length());

            String rawOutput = mistralAiClient.generateChatCompletion(systemPrompt, userPrompt);
            log.info("Received Mistral AI feature response (Length: {} chars)", rawOutput.length());

            String cleanJson = extractJson(rawOutput);
            JsonNode root = objectMapper.readTree(cleanJson);

            String featureName = root.path("featureName").asText("").trim();
            String description = root.path("description").asText("").trim();

            String requirements = formatSection(root.path("requirements"));
            String requestSpec = formatSection(root.path("requestSpecification"));
            String responseSpec = formatSection(root.path("responseSpecification"));
            String constraints = formatSection(root.path("constraints"));

            String endpoint = extractEndpoint(root, requestSpec, astResult);
            String httpMethod = extractHttpMethod(root, requestSpec);

            if (!featureName.isEmpty() && (!description.isEmpty() || !requirements.isEmpty())) {
                return FeatureGenerationResult.ok(
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
            }
        } catch (Exception ex) {
            log.warn("Mistral AI call was not completed: {}", ex.getMessage());
        }
        
        return null;
    }

    private String extractEndpoint(JsonNode root, String requestSpec, AstAnalysisResult astResult) {
        if (root != null && root.has("endpoint") && !root.path("endpoint").asText().trim().isEmpty()) {
            return root.path("endpoint").asText().trim();
        }
        if (requestSpec != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(POST|GET|PUT|DELETE|PATCH)\\s+([/a-zA-Z0-9_{}-]+)").matcher(requestSpec);
            if (matcher.find()) {
                return matcher.group(2).trim();
            }
        }
        if (astResult != null && astResult.getSourceCodeStructure() != null && astResult.getSourceCodeStructure().getEntities() != null && !astResult.getSourceCodeStructure().getEntities().isEmpty()) {
            return "/" + pluralize(astResult.getSourceCodeStructure().getEntities().get(0).getClassName());
        }
        return "/api/v1/resource";
    }

    private String extractHttpMethod(JsonNode root, String requestSpec) {
        if (root != null && root.has("httpMethod") && !root.path("httpMethod").asText().trim().isEmpty()) {
            return root.path("httpMethod").asText().trim().toUpperCase();
        }
        if (requestSpec != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(POST|GET|PUT|DELETE|PATCH)\\s+([/a-zA-Z0-9_{}-]+)").matcher(requestSpec);
            if (matcher.find()) {
                return matcher.group(1).trim().toUpperCase();
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
                    if (!text.matches("^\\d+\\..*")) {
                        sb.append(i + 1).append(". ").append(text).append("\\n");
                    } else {
                        sb.append(text).append("\\n");
                    }
                } else {
                    try {
                        sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item)).append("\\n");
                    } catch (Exception e) {
                        sb.append(item.toString()).append("\\n");
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

    private String pluralize(String name) {
        if (name == null || name.isEmpty()) return "items";
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("s")) return lower;
        if (lower.endsWith("y")) return lower.substring(0, lower.length() - 1) + "ies";
        return lower + "s";
    }
}
