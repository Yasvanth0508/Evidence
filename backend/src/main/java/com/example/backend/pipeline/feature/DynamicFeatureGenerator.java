package com.example.backend.pipeline.feature;

import com.example.backend.common.enums.Difficulty;
import com.example.backend.pipeline.analysis.dto.AstAnalysisResult;
import com.example.backend.pipeline.analysis.dto.ControllerInfo;
import com.example.backend.pipeline.analysis.dto.EntityInfo;
import com.example.backend.pipeline.analysis.dto.FieldInfo;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fallback feature generator that synthesizes a domain-grounded feature task
 * directly from AST metadata when Mistral AI is unavailable or returns invalid output.
 *
 * The generated task is scaled to the requested difficulty level:
 * - EASY:         Simple single-entity POST with 2–3 fields, 3 test cases
 * - INTERMEDIATE: Business-action PUT/GET with 3–4 fields, 5 test cases
 * - DIFFICULT:    Cross-entity POST with 4–5 fields, 7 test cases
 */
@Component
public class DynamicFeatureGenerator implements FeatureGenerator {

    @Override
    public FeatureGenerationResult generateFeature(UUID assessmentId, AstAnalysisResult astResult, Difficulty difficulty) {
        Difficulty d = difficulty != null ? difficulty : Difficulty.INTERMEDIATE;

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

        String primaryEntity   = !entities.isEmpty() ? entities.get(0).getClassName() : "Record";
        String secondaryEntity = entities.size() > 1 ? entities.get(1).getClassName() : "Activity";
        String primaryPlural   = pluralize(primaryEntity);

        // Collect non-ID fields of primary entity (up to 5)
        List<String> fields = new ArrayList<>();
        if (!entities.isEmpty() && entities.get(0).getFields() != null) {
            fields = entities.get(0).getFields().stream()
                    .filter(f -> !f.isId())
                    .map(FieldInfo::getName)
                    .limit(5)
                    .collect(Collectors.toList());
        }
        String f1 = fields.size() > 0 ? fields.get(0) : "name";
        String f2 = fields.size() > 1 ? fields.get(1) : "status";
        String f3 = fields.size() > 2 ? fields.get(2) : "description";

        return switch (d) {
            case EASY -> buildEasy(assessmentId, primaryEntity, primaryPlural, f1, f2);
            case INTERMEDIATE -> buildIntermediate(assessmentId, primaryEntity, primaryPlural, f1, f2, f3);
            case DIFFICULT -> buildDifficult(assessmentId, primaryEntity, secondaryEntity, primaryPlural, f1, f2, f3);
        };
    }

    // -----------------------------------------------------------------------
    // EASY: Simple create (POST), 2–3 fields, 3 test cases
    // -----------------------------------------------------------------------
    private FeatureGenerationResult buildEasy(UUID id, String entity, String plural, String f1, String f2) {
        String featureName  = "Create " + entity;
        String description  = "Allow clients to create a new " + entity + " by providing the required fields.";
        String requirements = "1. Accept a POST request at /" + plural + " with a JSON body.\n" +
                              "2. Validate that " + f1 + " is non-null and non-empty (max 255 chars).\n" +
                              "3. Persist the new " + entity + " using Spring Data JPA.\n" +
                              "4. Return the created " + entity + " with its generated ID and HTTP 201.";
        String requestBody  = "{\"" + f1 + "\": \"String — required, name of the " + entity + "\"," +
                               "\"" + f2 + "\": \"String — required, status or category\"}";
        String responseBody = "{\"id\": \"Long — generated identifier\"," +
                               "\"" + f1 + "\": \"String\"," +
                               "\"" + f2 + "\": \"String\"}";
        String constraints  = "Use @NotBlank on request fields. Return 201 on success, 400 on validation failure.";
        String testCaseSeed = buildTestCases(
                tc("Create " + entity + " successfully",
                   "Valid payload returns 201 with the created resource",
                   "{\"" + f1 + "\":\"Test " + entity + "\",\"" + f2 + "\":\"ACTIVE\"}",
                   201,
                   "{\"id\":1,\"" + f1 + "\":\"Test " + entity + "\",\"" + f2 + "\":\"ACTIVE\"}"),
                tc("Missing required field returns 400",
                   "Omitting " + f1 + " triggers a validation error",
                   "{\"" + f2 + "\":\"ACTIVE\"}",
                   400,
                   "{\"error\":\"Validation failed\",\"field\":\"" + f1 + "\"}"),
                tc("Empty " + f1 + " returns 400",
                   "Blank string for " + f1 + " is rejected",
                   "{\"" + f1 + "\":\"\",\"" + f2 + "\":\"ACTIVE\"}",
                   400,
                   "{\"error\":\"Validation failed\"}")
        );

        FeatureGenerationResult res = FeatureGenerationResult.ok(
                id, featureName, description, requirements,
                requestBody, responseBody, constraints,
                "dynamic-ast-synthesizer-easy", "/" + plural, "POST");
        res.setTestCaseSeed(testCaseSeed);
        return res;
    }

    // -----------------------------------------------------------------------
    // INTERMEDIATE: Status-update (PUT), 3–4 fields, 5 test cases
    // -----------------------------------------------------------------------
    private FeatureGenerationResult buildIntermediate(UUID id, String entity, String plural, String f1, String f2, String f3) {
        String featureName  = "Update " + entity + " Status";
        String description  = "Allow clients to transition the status of an existing " + entity + " through a defined workflow.";
        String requirements = "1. Accept PUT /" + plural + "/{id}/status with a JSON body containing the new status.\n" +
                              "2. Validate that the target " + entity + " exists (404 if not).\n" +
                              "3. Validate that the new status is a legal transition from the current status.\n" +
                              "4. Update and persist the " + entity + " using @Transactional.\n" +
                              "5. Return the updated " + entity + " with HTTP 200.";
        String requestBody  = "{\"" + f2 + "\": \"String — required, new status value\"," +
                               "\"reason\": \"String — optional, reason for the transition\"}";
        String responseBody = "{\"id\": \"Long — identifier\"," +
                               "\"" + f1 + "\": \"String\"," +
                               "\"" + f2 + "\": \"String — updated status\"," +
                               "\"updatedAt\": \"Instant\"}";
        String constraints  = "Use @Transactional. Validate status enum value. Return 200, 400, 404 appropriately.";
        String testCaseSeed = buildTestCases(
                tc("Update status successfully", "Valid transition returns 200", "{\"" + f2 + "\":\"COMPLETED\"}", 200,
                        "{\"id\":1,\"" + f2 + "\":\"COMPLETED\"}"),
                tc("Invalid status value returns 400", "Unrecognised status string", "{\"" + f2 + "\":\"UNKNOWN\"}", 400,
                        "{\"error\":\"Invalid status value\"}"),
                tc(entity + " not found returns 404", "Non-existent ID", "{\"" + f2 + "\":\"COMPLETED\"}", 404,
                        "{\"error\":\"" + entity + " not found\"}"),
                tc("Missing status field returns 400", "Body without status", "{}", 400,
                        "{\"error\":\"Validation failed\"}"),
                tc("Illegal state transition returns 409", "Transitioning from CANCELLED to ACTIVE",
                        "{\"" + f2 + "\":\"ACTIVE\"}", 409, "{\"error\":\"Invalid state transition\"}")
        );

        FeatureGenerationResult res = FeatureGenerationResult.ok(
                id, featureName, description, requirements,
                requestBody, responseBody, constraints,
                "dynamic-ast-synthesizer-intermediate", "/" + plural + "/{id}/status", "PUT");
        res.setTestCaseSeed(testCaseSeed);
        return res;
    }

    // -----------------------------------------------------------------------
    // DIFFICULT: Cross-entity create (POST), 4–5 fields, 7 test cases
    // -----------------------------------------------------------------------
    private FeatureGenerationResult buildDifficult(UUID id, String entity, String related, String plural, String f1, String f2, String f3) {
        String featureName  = "Create " + entity + " with " + related;
        String description  = "Allow clients to atomically create a new " + entity + " together with an associated " + related +
                              ", validating ownership and business invariants within a single transaction.";
        String requirements = "1. Accept POST /" + plural + "/full with a JSON body containing " + entity + " fields and a nested " + related.toLowerCase() + " object.\n" +
                              "2. Validate all required fields on both the " + entity + " and the nested " + related + ".\n" +
                              "3. Verify the caller owns the resource (403 if not).\n" +
                              "4. Persist both entities atomically via @Transactional.\n" +
                              "5. Return the created " + entity + " (with embedded " + related + " summary) and HTTP 201.";
        String requestBody  = "{\"" + f1 + "\": \"String — required\"," +
                               "\"" + f2 + "\": \"String — required\"," +
                               "\"" + f3 + "\": \"String — optional\"," +
                               "\"" + related.toLowerCase() + "\": {\"name\": \"String — required\", \"type\": \"String — required\"}}";
        String responseBody = "{\"id\": \"Long\"," +
                               "\"" + f1 + "\": \"String\"," +
                               "\"" + f2 + "\": \"String\"," +
                               "\"" + related.toLowerCase() + "Id\": \"Long — ID of created " + related + "\"," +
                               "\"createdAt\": \"Instant\"}";
        String constraints  = "Use @Transactional. Nested object requires @Valid + @NotNull. Return 201, 400, 403, 404, 409.";
        String testCaseSeed = buildTestCases(
                tc("Create " + entity + " with " + related + " successfully",
                        "Full valid payload returns 201",
                        "{\"" + f1 + "\":\"Value\",\"" + f2 + "\":\"Cat\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        201, "{\"id\":1,\"" + related.toLowerCase() + "Id\":10}"),
                tc("Missing nested " + related + " object returns 400",
                        "Body without the nested object",
                        "{\"" + f1 + "\":\"Value\",\"" + f2 + "\":\"Cat\"}", 400,
                        "{\"error\":\"Validation failed\",\"field\":\"" + related.toLowerCase() + "\"}"),
                tc("Invalid field value returns 400", "Negative or wrong-type field",
                        "{\"" + f1 + "\":\"\",\"" + f2 + "\":\"Cat\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        400, "{\"error\":\"Validation failed\"}"),
                tc("Related entity not found returns 404", "Referenced resource absent",
                        "{\"" + f1 + "\":\"Value\",\"" + f2 + "\":\"NONE\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        404, "{\"error\":\"" + related + " not found\"}"),
                tc("Duplicate entry returns 409", "Same " + f1 + " already exists",
                        "{\"" + f1 + "\":\"Existing\",\"" + f2 + "\":\"Cat\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        409, "{\"error\":\"Duplicate " + entity + "\"}"),
                tc("Boundary: max-length " + f1 + " accepted",
                        "String at exactly 255 chars should succeed",
                        "{\"" + f1 + "\":\"" + "A".repeat(255) + "\",\"" + f2 + "\":\"Cat\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        201, "{\"id\":2}"),
                tc("Unauthorized caller returns 403", "Caller does not own the parent resource",
                        "{\"" + f1 + "\":\"Value\",\"" + f2 + "\":\"Cat\",\"" + related.toLowerCase() + "\":{\"name\":\"R1\",\"type\":\"T1\"}}",
                        403, "{\"error\":\"Access denied\"}")
        );

        FeatureGenerationResult res = FeatureGenerationResult.ok(
                id, featureName, description, requirements,
                requestBody, responseBody, constraints,
                "dynamic-ast-synthesizer-difficult", "/" + plural + "/full", "POST");
        res.setTestCaseSeed(testCaseSeed);
        return res;
    }

    // -----------------------------------------------------------------------
    // Test case helpers
    // -----------------------------------------------------------------------

    private String tc(String name, String desc, String input, int status, String expected) {
        return String.format("{\"name\":%s,\"description\":%s,\"inputPayload\":%s,\"expectedStatus\":%d,\"expectedResponse\":%s}",
                quote(name), quote(desc), quote(input), status, quote(expected));
    }

    private String buildTestCases(String... cases) {
        return "[" + String.join(",", cases) + "]";
    }

    private String quote(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    private String pluralize(String name) {
        if (name == null || name.isEmpty()) return "items";
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("s")) return lower;
        if (lower.endsWith("y")) return lower.substring(0, lower.length() - 1) + "ies";
        return lower + "s";
    }
}
