package com.example.backend.pipeline.feature;

import com.example.backend.pipeline.analysis.dto.*;
import org.springframework.stereotype.Component;

@Component
public class FeaturePromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are a Principal Software Engineering Assessor and Spring Boot Architect.
                Your task is to analyze the AST metadata extracted from an existing repository and generate a realistic, production-ready feature task for a candidate to implement.
                
                CRITICAL REQUIREMENTS:
                1. The feature MUST directly build upon and integrate with the specific entities, repositories, and controllers extracted from this codebase.
                2. Do not invent unrelated domain concepts (e.g. if the repo is about Business/Orders/Products, design an Order/Inventory/Pricing extension; if it's about Student/Course, design an Academic extension).
                3. Provide clear HTTP REST request specifications, response specifications with status codes (200, 201, 400, 404), and exact JSON payloads.
                
                You must return a valid JSON object matching the exact schema below:
                {
                  "featureName": "Title of the feature extension",
                  "endpoint": "Primary REST endpoint (e.g. /enrollments or /api/v1/orders)",
                  "httpMethod": "Primary HTTP method (POST, GET, PUT, or DELETE)",
                  "description": "Comprehensive business and functional description of the feature to be built",
                  "requirements": "Numbered list of detailed technical requirements, entity changes, validations, and business logic",
                  "requestSpecification": "Expected REST endpoints, HTTP methods, headers, and JSON request payloads",
                  "responseSpecification": "Expected HTTP status codes (200, 201, 400, 404), response JSON structures, and error responses",
                  "constraints": "Engineering constraints (e.g. use JPA relations, validation annotations, transactional boundaries, HTTP codes)"
                }
                
                Return ONLY the valid JSON object. Do not include markdown code block formatting (```json) or extra text.
                """;
    }

    public String buildUserPrompt(AstAnalysisResult astResult) {
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
                            sb.append("      * ").append(f.getName()).append(" : ").append(f.getType()).append(rel).append(f.isId() ? " (@Id)" : "").append("\n");
                        }
                    }
                    if (e.getRelations() != null && !e.getRelations().isEmpty()) {
                        sb.append("      Relationships: ").append(String.join(", ", e.getRelations())).append("\n");
                    }
                }
            }

            sb.append("\n2. Extracted Controllers & REST Endpoints:\n");
            if (sc.getControllers() == null || sc.getControllers().isEmpty()) {
                sb.append("  (No Controllers detected)\n");
            } else {
                for (ControllerInfo c : sc.getControllers()) {
                    sb.append("  - Controller: ").append(c.getClassName()).append(" (Base Path: '").append(c.getBasePath()).append("')\n");
                    if (c.getEndpoints() != null) {
                        for (EndpointInfo ep : c.getEndpoints()) {
                            sb.append("      * [").append(ep.getHttpMethod()).append("] ").append(ep.getFullPath())
                                    .append(" -> method: ").append(ep.getHandlerMethod()).append("(), return: ").append(ep.getReturnType()).append("\n");
                        }
                    }
                }
            }

            sb.append("\n3. Extracted Repositories:\n");
            if (sc.getRepositories() != null && !sc.getRepositories().isEmpty()) {
                for (RepositoryInfo r : sc.getRepositories()) {
                    sb.append("  - Repository: ").append(r.getInterfaceName()).append(" for Entity: ").append(r.getDomainEntity()).append("\n");
                    if (r.getMethods() != null) {
                        for (String sig : r.getMethods()) {
                            sb.append("      * ").append(sig).append("\n");
                        }
                    }
                }
            }
        }

        sb.append("\n=== TASK ===\n");
        sb.append("Generate a comprehensive, domain-aligned feature extension task that candidates must implement. Build directly upon the extracted entities and endpoints shown above. Output as valid JSON.");
        return sb.toString();
    }
}
