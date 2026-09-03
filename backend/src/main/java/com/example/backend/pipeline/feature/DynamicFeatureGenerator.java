package com.example.backend.pipeline.feature;

import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.analysis.dto.EntityInfo;
import com.example.backend.pipeline.analysis.dto.ControllerInfo;
import com.example.backend.pipeline.analysis.dto.FieldInfo;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DynamicFeatureGenerator implements FeatureGenerator {

    @Override
    public FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult) {
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
