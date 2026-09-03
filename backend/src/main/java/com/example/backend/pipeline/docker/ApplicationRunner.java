package com.example.backend.pipeline.docker;

import com.example.backend.pipeline.docker.dto.DockerValidationResult;

import java.nio.file.Path;
import java.util.UUID;

public interface ApplicationRunner {
    DockerValidationResult runApplication(UUID assessmentId, Path targetBackendDir, BuildResult buildResult);
    boolean isAvailable();
}
