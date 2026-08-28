package com.example.backend.pipeline.analysis;

import com.example.backend.pipeline.analysis.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AstAnalysisServiceTest {

    @Test
    @DisplayName("Phase 3: Extract AST metadata from real Student-Management-System repository")
    void testAstAnalysisOnRealRepo() {
        AstAnalysisService astService = new AstAnalysisService(null, null);

        UUID assessmentId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Path repoRoot = Paths.get(System.getProperty("user.dir"), "storage", "assessments", assessmentId.toString(), "original");
        String subDirectory = "github_practice";

        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(repoRoot), "Cloned repository from Phase 1 must exist on disk");

        System.out.println("==================================================");
        System.out.println("STARTING PHASE 3: AST CODEBASE METADATA EXTRACTION");
        System.out.println("Repository Root: " + repoRoot.toAbsolutePath());
        System.out.println("Subdirectory:    " + subDirectory);
        System.out.println("==================================================");

        AstAnalysisResult result = astService.analyzeSourceCode(assessmentId, repoRoot, subDirectory);

        System.out.println("==================================================");
        System.out.println("PHASE 3 EXECUTION OUTCOME:");
        System.out.println("Status Success:     " + result.isSuccess());
        assertNull(result.getErrorMessage(), "Should not have error: " + result.getErrorMessage());
        assertTrue(result.isSuccess(), "AST Analysis must succeed");

        ProjectStructureDto ps = result.getProjectStructure();
        SourceCodeStructureDto sc = result.getSourceCodeStructure();
        ContentDetailsDto cd = result.getContentDetails();
        assertNotNull(cd, "ContentDetails must not be null");

        System.out.println("Language / Version: " + ps.getLanguage() + " " + ps.getJavaVersion());
        System.out.println("Framework:          Spring Boot " + ps.getSpringBootVersion());
        System.out.println("Total Java Files:   " + ps.getTotalJavaFiles());
        System.out.println("Dependencies Found: " + ps.getDependencies().size());
        System.out.println("--------------------------------------------------");
        System.out.println("TOTAL CONTROLLERS:  " + sc.getControllers().size());
        for (ControllerInfo ctrl : sc.getControllers()) {
            System.out.println("  * " + ctrl.getClassName() + " (Base Path: " + ctrl.getBasePath() + ")");
            for (EndpointInfo ep : ctrl.getEndpoints()) {
                System.out.println("     -> [" + ep.getHttpMethod() + "] " + ep.getFullPath() + " => " + ep.getHandlerMethod() + " (" + ep.getReturnType() + ")");
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("TOTAL ENTITIES:     " + sc.getEntities().size());
        for (EntityInfo entity : sc.getEntities()) {
            System.out.println("  * " + entity.getClassName() + " (Table: " + entity.getTableName() + ")");
            for (FieldInfo field : entity.getFields()) {
                String idMarker = field.isId() ? " [PRIMARY KEY]" : "";
                String relMarker = field.getRelation() != null ? " [" + field.getRelation() + "]" : "";
                System.out.println("     - " + field.getName() + ": " + field.getType() + idMarker + relMarker);
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("TOTAL REPOSITORIES: " + sc.getRepositories().size());
        for (RepositoryInfo repo : sc.getRepositories()) {
            System.out.println("  * " + repo.getInterfaceName() + " (Domain: " + repo.getDomainEntity() + ", ID: " + repo.getIdType() + ")");
        }

        System.out.println("--------------------------------------------------");
        System.out.println("TOTAL SERVICES:     " + sc.getServices().size());
        for (ServiceInfo svc : sc.getServices()) {
            System.out.println("  * " + svc.getClassName() + " (Methods: " + svc.getMethods().size() + ")");
            for (String method : svc.getMethods()) {
                System.out.println("     - " + method);
            }
        }

        System.out.println("==================================================");
        System.out.println("GENERATED JSON PAYLOADS PREVIEW:");
        System.out.println("--- PROJECT STRUCTURE JSON ---");
        System.out.println(result.getProjectStructureJson().substring(0, Math.min(300, result.getProjectStructureJson().length())) + "...");
        System.out.println("--- CONTENT DETAILS JSON ---");
        System.out.println(result.getContentDetailsJson());
        System.out.println("==================================================");
    }
}
