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

    private final MistralAiClient mistralAiClient;
    private final MistralAiConfig mistralAiConfig;
    private final FeaturePromptBuilder promptBuilder;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public FeatureSpecificationService(MistralAiClient mistralAiClient,
                                       MistralAiConfig mistralAiConfig,
                                       FeaturePromptBuilder promptBuilder,
                                       FeatureSpecificationRepository featureSpecificationRepository,
                                       AssessmentRepository assessmentRepository) {
        this.mistralAiClient = mistralAiClient;
        this.mistralAiConfig = mistralAiConfig;
        this.promptBuilder = promptBuilder;
        this.featureSpecificationRepository = featureSpecificationRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes Phase 4: Generates AI Feature Specification using Mistral AI (or dynamic AST domain synthesizer).
     */
    @Transactional
    public FeatureGenerationResult generateFeatureSpecification(UUID assessmentId, AstAnalysisResult astResult) {
        log.info("Phase 4: Starting AI Feature Specification Generation for Assessment {}", assessmentId);

        String featureName = null;
        String description = null;
        String requirements = null;
        String requestSpec = null;
        String responseSpec = null;
        String constraints = null;
        String rawOutput = null;

        // 1. Attempt LLM Generation via Mistral AI with structured AST prompt
        if (mistralAiConfig != null && mistralAiConfig.isConfigured()) {
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt();
                String userPrompt = promptBuilder.buildUserPrompt(astResult);

                log.info("Invoking Mistral AI ({}) with structured AST codebase prompt (length: {} chars)...",
                        mistralAiConfig.getModel(), userPrompt.length());

                rawOutput = mistralAiClient.generateChatCompletion(systemPrompt, userPrompt);
                log.info("Received Mistral AI feature response (Length: {} chars)", rawOutput.length());

                String cleanJson = extractJson(rawOutput);
                JsonNode root = objectMapper.readTree(cleanJson);

                featureName = root.path("featureName").asText("").trim();
                description = root.path("description").asText("").trim();

                // Format complex sections (Array / Object / String) safely without empty string loss
                requirements = formatSection(root.path("requirements"));
                requestSpec = formatSection(root.path("requestSpecification"));
                responseSpec = formatSection(root.path("responseSpecification"));
                constraints = formatSection(root.path("constraints"));

                String endpoint = extractEndpoint(root, requestSpec, astResult);
                String httpMethod = extractHttpMethod(root, requestSpec);

                if (!featureName.isEmpty() && (!description.isEmpty() || !requirements.isEmpty())) {
                    log.info("Successfully parsed Mistral AI Feature Specification: '{}' (Method: {}, Endpoint: {})",
                            featureName, httpMethod, endpoint);

                    persistFeatureSpecification(assessmentId, featureName, description, requirements, requestSpec, responseSpec, constraints, endpoint, httpMethod);

                    log.info("Phase 4: Feature Specification Generation COMPLETED for Assessment {} (Feature: {})", assessmentId, featureName);

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
                log.warn("Mistral AI call was not completed: {}. Falling back to dynamic AST domain synthesizer.", ex.getMessage());
            }
        } else {
            log.info("Mistral AI API is in offline mode. Utilizing dynamic AST domain synthesizer.");
        }

        // 2. Dynamic AST Domain Synthesizer (Driven purely by AST extracted Entities & Controllers)
        FeatureGenerationResult synthesized = synthesizeFeatureFromAst(assessmentId, astResult);
        featureName = synthesized.getFeatureName();
        description = synthesized.getDescription();
        requirements = synthesized.getRequirements();
        requestSpec = synthesized.getRequestSpecification();
        responseSpec = synthesized.getResponseSpecification();
        constraints = synthesized.getConstraints();
        rawOutput = synthesized.getRawModelOutput();
        String endpoint = synthesized.getEndpoint();
        String httpMethod = synthesized.getHttpMethod();

        // 3. Persist to PostgreSQL Database
        persistFeatureSpecification(assessmentId, featureName, description, requirements, requestSpec, responseSpec, constraints, endpoint, httpMethod);

        log.info("Phase 4: Feature Specification Generation COMPLETED for Assessment {} (Feature: {})", assessmentId, featureName);

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
                        sb.append(i + 1).append(". ").append(text).append("\n");
                    } else {
                        sb.append(text).append("\n");
                    }
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

    /**
     * Purely dynamic domain synthesizer constructed directly from extracted AST metadata.
     */
    public FeatureGenerationResult synthesizeFeatureFromAst(UUID assessmentId, AstAnalysisResult astResult) {
        List<EntityInfo> entities = new ArrayList<>();
        List<ControllerInfo> controllers = new ArrayList<>();

        if (astResult != null && astResult.getSourceCodeStructure() != null) {
            if (astResult.getSourceCodeStructure().getEntities() != null) {
                entities.addAll(astResult.getSourceCodeStructure().getEntities());
            }
            if (astResult.getSourceCodeStructure().getControllers() != null) {
                controllers.addAll(astResult.getSourceCodeStructure().getControllers());
            }
        }

        String primaryEntity = !entities.isEmpty() ? entities.get(0).getClassName() : "Record";
        String secondaryEntity = entities.size() > 1 ? entities.get(1).getClassName() : "Activity";
        String thirdEntity = entities.size() > 2 ? entities.get(2).getClassName() : null;

        String primaryPlural = pluralize(primaryEntity);
        String secondaryPlural = pluralize(secondaryEntity);

        // Collect fields of primary entity
        List<String> primaryFields = new ArrayList<>();
        if (!entities.isEmpty() && entities.get(0).getFields() != null) {
            primaryFields = entities.get(0).getFields().stream()
                    .filter(f -> !f.isId())
                    .map(FieldInfo::getName)
                    .collect(Collectors.toList());
        }
        String sampleField1 = primaryFields.size() > 0 ? primaryFields.get(0) : "name";
        String sampleField2 = primaryFields.size() > 1 ? primaryFields.get(1) : "status";

        String featureName = primaryEntity + " Operations & " + secondaryEntity + " Processing Engine";
        String description = "Implement an end-to-end management, workflow orchestration, and analytics extension for the " +
                primaryEntity + " and " + secondaryEntity + (thirdEntity != null ? " and " + thirdEntity : "") +
                " domain models, ensuring relational data consistency, input validation, and business logic execution.";

        String requirements = String.format("""
                1. Establish relational association between %s and %s with cascade rules and timestamp tracking.
                2. Implement POST /%s to create a new %s with validation ensuring %s is non-empty and valid.
                3. Implement GET /%s/{id}/%s to retrieve all associated %s belonging to a specific %s.
                4. Implement PUT /%s/{id}/status to transition processing status (ACTIVE, COMPLETED, SUSPENDED, CANCELLED).
                5. Implement GET /%s/{id}/summary to calculate aggregated domain metrics and summary statistics.
                """,
                primaryEntity, secondaryEntity,
                primaryPlural, primaryEntity, sampleField1,
                primaryPlural, secondaryPlural, secondaryEntity, primaryEntity,
                primaryPlural,
                primaryPlural
        );

        String requestSpec = String.format("""
                POST /%s
                Headers: Content-Type: application/json
                Body:
                {
                  "%s": "Sample %s Entry",
                  "%s": "ACTIVE"
                }

                PUT /%s/{id}/status
                Headers: Content-Type: application/json
                Body:
                {
                  "status": "COMPLETED"
                }
                """,
                primaryPlural, sampleField1, primaryEntity, sampleField2, primaryPlural
        );

        String responseSpec = String.format("""
                POST /%s:
                  201 Created: Returns created %s object with generated ID and timestamps.
                  400 Bad Request: If %s is missing or invalid payload.

                GET /%s/{id}/%s:
                  200 OK: Returns array of %s items linked to the %s ID.
                  404 Not Found: If %s with specified ID does not exist.
                """,
                primaryPlural, primaryEntity, sampleField1,
                primaryPlural, secondaryPlural, secondaryEntity, primaryEntity, primaryEntity
        );

        String constraints = """
                1. Follow Spring Boot MVC architectural standards (Controller -> Service -> Repository).
                2. Use Spring Data JPA transactions (@Transactional) for state mutation methods.
                3. Apply Jakarta Bean Validation (@NotNull, @Size, @Positive) on request payloads.
                4. Ensure appropriate HTTP status codes (200, 201, 400, 404) on all responses.
                """;

        return FeatureGenerationResult.ok(
                assessmentId,
                featureName,
                description,
                requirements,
                requestSpec,
                responseSpec,
                constraints,
                "dynamic-ast-synthesizer-v2",
                "/" + primaryPlural,
                "POST"
        );
    }

    private String pluralize(String name) {
        if (name == null || name.isEmpty()) return "items";
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("s")) return lower;
        if (lower.endsWith("y")) return lower.substring(0, lower.length() - 1) + "ies";
        return lower + "s";
    }
}
