package com.example.backend.pipeline.docker;

import com.example.backend.pipeline.docker.dto.DockerValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DockerValidationServiceTest {

    @Test
    @DisplayName("Phase 2: Validate Docker build & runnable status on real user repo")
    void testDockerValidationOnRealRepo() {
        DockerCommandExecutor dockerExecutor = new DockerCommandExecutor();
        DockerValidationService dockerService = new DockerValidationService(dockerExecutor);

        UUID assessmentId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Path repoRoot = Paths.get(System.getProperty("user.dir"), "storage", "assessments", assessmentId.toString(), "original");
        String subDirectory = "github_practice";

        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(repoRoot), "Cloned repository from Phase 1 must exist on disk");

        System.out.println("==================================================");
        System.out.println("STARTING PHASE 2: DOCKER BUILD & RUNNABLE VALIDATION");
        System.out.println("Repository Root: " + repoRoot.toAbsolutePath());
        System.out.println("Subdirectory:    " + subDirectory);
        System.out.println("==================================================");

        DockerValidationResult result = dockerService.validateProjectRunnable(assessmentId, repoRoot, subDirectory);

        System.out.println("==================================================");
        System.out.println("PHASE 2 EXECUTION OUTCOME:");
        System.out.println("Status Success:     " + result.isSuccess());
        System.out.println("Build Status:       " + result.getBuildStatus());
        System.out.println("Application Status: " + result.getApplicationStatus());
        System.out.println("Image Tag:          " + result.getImageTag());
        System.out.println("Build Duration:     " + result.getBuildDurationMs() + " ms");
        System.out.println("Startup Duration:   " + result.getStartupDurationMs() + " ms");
        if (result.getFailureReason() != null) {
            System.out.println("Failure Reason:     " + result.getFailureReason());
        }
        System.out.println("==================================================");

        assertNotNull(result.getBuildStatus(), "Build status must be determined");
        assertNotNull(result.getApplicationStatus(), "Application status must be determined");
    }
}
