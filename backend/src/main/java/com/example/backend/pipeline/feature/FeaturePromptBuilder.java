package com.example.backend.pipeline.feature;

import com.example.backend.common.enums.Difficulty;
import com.example.backend.pipeline.analysis.dto.*;
import org.springframework.stereotype.Component;

@Component
public class FeaturePromptBuilder {

    // -----------------------------------------------------------------------
    // Public API — difficulty-aware (primary)
    // -----------------------------------------------------------------------

    public String buildSystemPrompt(Difficulty difficulty) {
        Difficulty d = difficulty != null ? difficulty : Difficulty.INTERMEDIATE;
        return BASE_SYSTEM_PROMPT + "\n\n" + difficultyContract(d);
    }

    public String buildUserPrompt(AstAnalysisResult astResult, Difficulty difficulty) {
        return buildAstSection(astResult) +
                "\n=== TASK ===\n" +
                "Generate a difficulty-appropriate, domain-aligned feature task for difficulty level: " +
                (difficulty != null ? difficulty.name() : "INTERMEDIATE") +
                ".\nBuild directly upon the extracted entities and endpoints shown above. Output as valid JSON only.";
    }

    // -----------------------------------------------------------------------
    // Backward-compatible zero-arg overloads (delegates to INTERMEDIATE)
    // -----------------------------------------------------------------------

    public String buildSystemPrompt() {
        return buildSystemPrompt(Difficulty.INTERMEDIATE);
    }

    public String buildUserPrompt(AstAnalysisResult astResult) {
        return buildUserPrompt(astResult, Difficulty.INTERMEDIATE);
    }

    // -----------------------------------------------------------------------
    // Base system prompt (shared across all difficulties)
    // -----------------------------------------------------------------------

    private static final String BASE_SYSTEM_PROMPT = """
            You are a Principal Software Engineering Assessor and Spring Boot Architect.
            Your task is to analyze the AST metadata extracted from an existing repository and generate a realistic,
            production-ready feature task for a candidate to implement.

            CORE RULES — APPLY REGARDLESS OF DIFFICULTY:
            1. The feature MUST directly build upon the specific entities, repositories, and controllers extracted from the codebase below.
            2. Do NOT invent unrelated domain concepts. Stay within the domain of the extracted entities.
            3. Choose EXACTLY ONE new endpoint that does NOT already exist in the listed controllers.
            4. The endpoint path MUST contain a segment matching one of the extracted entity names or their plural forms.

            You MUST return a valid JSON object matching this exact schema — nothing else, no markdown fences:
            {
              "featureName": "Concise title, e.g. 'Create Note'",
              "description": "1-2 sentence business context",
              "endpoint": "Single REST path starting with /, e.g. /api/notes",
              "httpMethod": "Exactly one of: POST, GET, PUT, DELETE, PATCH",
              "requestBody": {
                "<fieldName>": "<JavaType> — <brief description>"
              },
              "responseBody": {
                "<fieldName>": "<JavaType> — <brief description>"
              },
              "requirements": "Numbered list of implementation steps",
              "constraints": "Engineering constraints (JPA, validation annotations, HTTP status codes)",
              "testCases": [
                {
                  "name": "string",
                  "description": "string",
                  "inputPayload": "JSON string (or empty string for no body)",
                  "expectedStatus": 200,
                  "expectedResponse": "JSON string"
                }
              ]
            }
            """;

    // -----------------------------------------------------------------------
    // Per-difficulty contract injected after base prompt
    // -----------------------------------------------------------------------

    private String difficultyContract(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> """
                    === DIFFICULTY CONTRACT: EASY ===

                    ENDPOINT SELECTION:
                    • Choose a simple create (POST) or read (GET by ID or list) operation on ONE single entity.
                    • No joins, no state machines, no cross-entity logic.

                    REQUEST BODY:
                    • Include EXACTLY 2–3 flat fields (String, Long, or Boolean). All required.
                    • Use the exact field names from the entity definition above.
                    • Do NOT include the @Id field — it is server-generated.

                    RESPONSE BODY:
                    • Include EXACTLY 3–4 flat fields. MUST include the entity's @Id field.
                    • No nested objects.

                    TEST CASES — Generate EXACTLY 3:
                    1. Happy path: valid input returns 200 or 201.
                    2. Validation error: one required field is missing, returns 400.
                    3. Not-found or conflict: the target resource does not exist (404) or already exists (409).
                    """;

            case INTERMEDIATE -> """
                    === DIFFICULTY CONTRACT: INTERMEDIATE ===

                    ENDPOINT SELECTION:
                    • Choose a business-action endpoint: a state change (PUT/PATCH), a filtered list (GET with query params),
                      or a derived/computed result on a single entity.
                    • Must NOT duplicate any endpoint already listed in the controllers above.

                    REQUEST BODY:
                    • Include 3–4 fields. At most 1 optional field (mark it "optional" in the description).
                    • No deep nesting. Use exact entity field names.

                    RESPONSE BODY:
                    • Include 3–5 fields. MUST include the entity's @Id field.
                    • May include ONE computed/derived field not stored on the entity (e.g. "totalCount", "isOverdue").

                    TEST CASES — Generate EXACTLY 5:
                    1. Happy path: valid input, expected success response.
                    2. Invalid state transition or business rule violation (400 or 409).
                    3. Target entity not found (404).
                    4. Missing or invalid required field (400).
                    5. Edge case: empty result set, boundary value, or duplicate entry.
                    """;

            case DIFFICULT -> """
                    === DIFFICULTY CONTRACT: DIFFICULT ===

                    ENDPOINT SELECTION:
                    • Choose a complex operation spanning TWO related entities, or requiring transactional logic
                      and non-trivial computation (aggregation, batch update, or ownership check).
                    • Must NOT duplicate any endpoint already listed in the controllers above.

                    REQUEST BODY:
                    • Include 4–5 fields. Exactly 1 nested object is allowed (with at most 2 sub-fields).
                    • Use exact field names from the entity definitions above.

                    RESPONSE BODY:
                    • Include 4–6 fields. MUST include the primary entity's @Id field.
                    • May include aggregated or computed values (e.g. "itemCount", "totalAmount").

                    TEST CASES — Generate EXACTLY 7:
                    1. Happy path: full valid payload, expected success.
                    2. Partial payload — nested object missing entirely (400).
                    3. Invalid field value (e.g. negative number, wrong enum) (400).
                    4. Referenced related entity not found (404).
                    5. Business invariant violation (409 or 422).
                    6. Boundary/edge case (empty list, zero value, max-length string).
                    7. Authorization/ownership violation — caller does not own the resource (403).
                    """;
        };
    }

    // -----------------------------------------------------------------------
    // AST section builder (shared)
    // -----------------------------------------------------------------------

    private String buildAstSection(AstAnalysisResult astResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EXTRACTED CODEBASE AST ARCHITECTURE METADATA ===\n\n");

        if (astResult != null && astResult.getProjectStructure() != null) {
            ProjectStructureDto ps = astResult.getProjectStructure();
            sb.append("Project Language: ").append(ps.getLanguage()).append(" (Java ").append(ps.getJavaVersion()).append(")\n");
            sb.append("Framework: Spring Boot ").append(ps.getSpringBootVersion()).append("\n");
            if (ps.getDependencies() != null && !ps.getDependencies().isEmpty()) {
                sb.append("Key Dependencies: ").append(String.join(", ", ps.getDependencies())).append("\n\n");
            }
        }

        if (astResult != null && astResult.getSourceCodeStructure() != null) {
            SourceCodeStructureDto sc = astResult.getSourceCodeStructure();

            sb.append("1. Extracted Entities & Domain Models:\n");
            if (sc.getEntities() == null || sc.getEntities().isEmpty()) {
                sb.append("  (No JPA entities detected)\n");
            } else {
                for (EntityInfo e : sc.getEntities()) {
                    sb.append("  - Entity: ").append(e.getClassName()).append(" (Table: ").append(e.getTableName()).append(")\n");
                    if (e.getFields() != null) {
                        for (FieldInfo f : e.getFields()) {
                            String rel = f.getRelation() != null ? " [" + f.getRelation() + " -> " + f.getTargetEntity() + "]" : "";
                            sb.append("      * ").append(f.getName()).append(" : ").append(f.getType())
                                    .append(rel).append(f.isId() ? " (@Id)" : "").append("\n");
                        }
                    }
                    if (e.getRelations() != null && !e.getRelations().isEmpty()) {
                        sb.append("      Relationships: ").append(String.join(", ", e.getRelations())).append("\n");
                    }
                }
            }

            sb.append("\n2. Extracted Controllers & Existing REST Endpoints (DO NOT DUPLICATE these):\n");
            if (sc.getControllers() == null || sc.getControllers().isEmpty()) {
                sb.append("  (No Controllers detected)\n");
            } else {
                for (ControllerInfo c : sc.getControllers()) {
                    sb.append("  - Controller: ").append(c.getClassName())
                            .append(" (Base Path: '").append(c.getBasePath()).append("')\n");
                    if (c.getEndpoints() != null) {
                        for (EndpointInfo ep : c.getEndpoints()) {
                            sb.append("      * [").append(ep.getHttpMethod()).append("] ").append(ep.getFullPath())
                                    .append(" -> method: ").append(ep.getHandlerMethod())
                                    .append("(), return: ").append(ep.getReturnType()).append("\n");
                        }
                    }
                }
            }

            sb.append("\n3. Extracted Repositories:\n");
            if (sc.getRepositories() != null && !sc.getRepositories().isEmpty()) {
                for (RepositoryInfo r : sc.getRepositories()) {
                    sb.append("  - Repository: ").append(r.getInterfaceName())
                            .append(" for Entity: ").append(r.getDomainEntity()).append("\n");
                    if (r.getMethods() != null) {
                        for (String sig : r.getMethods()) {
                            sb.append("      * ").append(sig).append("\n");
                        }
                    }
                }
            }
        }

        return sb.toString();
    }
}
