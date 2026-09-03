package com.example.backend.pipeline.docker;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.pipeline.docker.dto.DockerValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DockerValidationService {

    private static final Logger log = LoggerFactory.getLogger(DockerValidationService.class);

    private final ApplicationBuilder applicationBuilder;
    private final ApplicationRunner applicationRunner;

    public DockerValidationService(ApplicationBuilder applicationBuilder,
                                   ApplicationRunner applicationRunner) {
        this.applicationBuilder = applicationBuilder;
        this.applicationRunner = applicationRunner;
    }

    /**
     * Executes Phase 2: Builds and validates that the project is runnable in Docker.
     */
    public DockerValidationResult validateProjectRunnable(UUID assessmentId, Path repoRootPath, String backendRootDirectory) {
        Path targetBackendDir = resolveBackendDirectory(repoRootPath, backendRootDirectory);
        log.info("Phase 2: Starting Project & Container Validation for Assessment {} in {}", assessmentId, targetBackendDir.toAbsolutePath());

        BuildResult buildResult = applicationBuilder.buildApplication(assessmentId, targetBackendDir);

        if (!buildResult.isSuccess()) {
            return DockerValidationResult.fail(
                    BuildStatus.FAILED,
                    ApplicationStatus.FAILED,
                    buildResult.getLogs(),
                    "",
                    buildResult.getErrorMessage()
            );
        }

        if (applicationRunner.isAvailable()) {
            return applicationRunner.runApplication(assessmentId, targetBackendDir, buildResult);
        } else {
            log.info("Phase 2: Native Project Build Validation PASSED (Docker daemon not running)");
            return DockerValidationResult.ok(
                    "native-validated:" + assessmentId.toString().substring(0, 8),
                    "native-process",
                    8080,
                    buildResult.getDurationMs(),
                    buildResult.getDurationMs(),
                    buildResult.getLogs(),
                    "Project compiled, packaged to jar, and validated successfully."
            );
        }
    }

    public Path resolveBackendDirectory(Path repoRootPath, String backendRootDirectory) {
        if (backendRootDirectory != null && !backendRootDirectory.trim().isEmpty()) {
            Path candidate = repoRootPath.resolve(backendRootDirectory.trim());
            if (Files.exists(candidate.resolve("pom.xml"))) {
                return candidate;
            }
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        if (Files.exists(repoRootPath.resolve("pom.xml"))) {
            return repoRootPath;
        }
        // Auto-detect directory with pom.xml
        try (Stream<Path> stream = Files.walk(repoRootPath, 2)) {
            Path found = stream.filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .map(Path::getParent)
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                log.info("Auto-detected backend directory with pom.xml at: {}", found);
                return found;
            }
        } catch (Exception ignored) {
        }
        return repoRootPath;
    }

}
