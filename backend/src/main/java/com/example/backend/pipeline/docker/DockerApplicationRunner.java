package com.example.backend.pipeline.docker;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.pipeline.docker.dto.DockerValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class DockerApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerApplicationRunner.class);
    private final ProcessCommandExecutor dockerExecutor;

    public DockerApplicationRunner(ProcessCommandExecutor dockerExecutor) {
        this.dockerExecutor = dockerExecutor;
    }

    @Override
    public boolean isAvailable() {
        return dockerExecutor.executeCommand(new File(System.getProperty("user.dir")), 10, "docker", "info").isSuccess();
    }

    @Override
    public DockerValidationResult runApplication(UUID assessmentId, Path targetBackendDir, BuildResult buildResult) {
        File workingDir = targetBackendDir.toFile();
        String tag = "evidence-assessment-" + assessmentId.toString().substring(0, 8) + ":test";
        String containerName = "evidence-test-" + assessmentId.toString().substring(0, 8);

        log.info("Building Docker image with tag {}", tag);
        ProcessCommandExecutor.ProcessResult buildCmdResult = dockerExecutor.executeCommand(
                workingDir,
                120,
                "docker", "build", "-t", tag, "."
        );

        if (!buildCmdResult.isSuccess()) {
            cleanupContainerAndImage(containerName, tag);
            return DockerValidationResult.fail(
                    BuildStatus.FAILED,
                    ApplicationStatus.FAILED,
                    buildResult.getLogs() + "\n" + buildCmdResult.combinedOutput(),
                    "",
                    "Docker image build failed: " + buildCmdResult.stderr()
            );
        }

        cleanupContainer(containerName);

        log.info("Running Docker container {}", containerName);
        long runStartTime = System.currentTimeMillis();
        ProcessCommandExecutor.ProcessResult runResult = dockerExecutor.executeCommand(
                workingDir,
                30,
                "docker", "run", "-d", "--name", containerName, "-p", "8080:8080", tag
        );

        if (!runResult.isSuccess()) {
            String logs = getContainerLogs(containerName);
            cleanupContainerAndImage(containerName, tag);
            return DockerValidationResult.fail(
                    BuildStatus.SUCCESS,
                    ApplicationStatus.FAILED,
                    buildResult.getLogs() + "\n" + buildCmdResult.combinedOutput(),
                    logs,
                    "Container failed to start: " + runResult.stderr()
            );
        }

        String containerId = runResult.stdout().trim();
        log.info("Container started with ID: {}", containerId);

        boolean healthy = waitForPort(8080, 20000);
        String appLogs = getContainerLogs(containerName);
        long runDurationMs = System.currentTimeMillis() - runStartTime;

        cleanupContainerAndImage(containerName, tag);

        if (!healthy) {
            log.error("Phase 2: Container started but application failed to initialize on port 8080");
            return DockerValidationResult.fail(
                    BuildStatus.SUCCESS,
                    ApplicationStatus.FAILED,
                    buildResult.getLogs() + "\n" + buildCmdResult.combinedOutput(),
                    appLogs,
                    "Application failed to bind to port 8080 within timeout."
            );
        }

        log.info("Phase 2: Project & Container Validation PASSED for Assessment {}", assessmentId);
        return DockerValidationResult.ok(
                containerId,
                tag,
                8080,
                buildResult.getDurationMs(),
                runDurationMs,
                buildResult.getLogs() + "\n" + buildCmdResult.combinedOutput() + "\n--- App Logs ---\n" + appLogs,
                "Build and container execution successful."
        );
    }

    private void cleanupContainerAndImage(String containerName, String imageName) {
        cleanupContainer(containerName);
        log.debug("Removing image {}", imageName);
        dockerExecutor.executeCommand(new File(System.getProperty("user.dir")), 15, "docker", "rmi", "-f", imageName);
    }

    private void cleanupContainer(String containerName) {
        log.debug("Stopping and removing container {}", containerName);
        dockerExecutor.executeCommand(new File(System.getProperty("user.dir")), 15, "docker", "rm", "-f", containerName);
    }

    private String getContainerLogs(String containerName) {
        return dockerExecutor.executeCommand(new File(System.getProperty("user.dir")), 10, "docker", "logs", containerName).combinedOutput();
    }

    private boolean waitForPort(int port, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                try (java.net.Socket s = new java.net.Socket("localhost", port)) {
                    return true;
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}
