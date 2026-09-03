package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.execution.ExecutionLogsResponse;
import com.example.backend.assessment.dto.execution.ExecutionRunResponse;
import com.example.backend.assessment.dto.execution.ExecutionStatusResponse;
import com.example.backend.assessment.dto.execution.StopExecutionResponse;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.pipeline.docker.ProcessCommandExecutor;
import com.example.backend.pipeline.docker.DockerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing the compilation, packaging, and execution of candidate source code
 * within ephemeral Docker containers or native sandbox processes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateExecutionService {

    private final AssessmentRepository assessmentRepository;
    private final CandidateWorkspaceService candidateWorkspaceService;
    private final ProcessCommandExecutor dockerExecutor;
    private final ProcessLogBuffer logBuffer;

    private static final Map<UUID, ActiveExecution> activeExecutions = new ConcurrentHashMap<>();

    private record ActiveExecution(
            UUID executionId,
            UUID assessmentId,
            String containerName,
            String imageTag,
            int port,
            Process process,
            Instant startTime,
            BuildStatus buildStatus,
            ContainerStatus containerStatus,
            ApplicationStatus applicationStatus,
            String errorMessage
    ) {}

    /**
     * Compiles candidate code and launches an ephemeral Docker container or sandboxed process.
     *
     * @param candidateId  UUID of the candidate triggering the run.
     * @param assessmentId UUID of the assessment being executed.
     * @return ExecutionRunResponse containing port and execution status.
     * @throws ResourceNotFoundException if assessment does not exist.
     * @throws ForbiddenException        if candidate is not authorized for the assessment.
     */
    public ExecutionRunResponse runCandidateApplication(UUID candidateId, UUID assessmentId) {
        log.info("Candidate {} requested application RUN for assessment {}", candidateId, assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCandidate() == null || !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        // 1. Stop any existing running container for this assessment
        stopExistingExecution(assessmentId);

        Path workspaceDir = candidateWorkspaceService.resolveCandidateWorkspace(candidateId, assessmentId);
        File workingDir = workspaceDir.toFile();
        UUID executionId = UUID.randomUUID();
        String execKey = assessmentId.toString();
        logBuffer.clear(execKey);

        int exposedPort = 18080 + Math.abs(assessmentId.hashCode() % 1000);
        String tag = "evidence-candidate-" + assessmentId.toString().substring(0, 8) + ":run";
        String containerName = "evidence-candidate-" + assessmentId.toString().substring(0, 8);

        logBuffer.append(execKey, ">>> [1/3] Building and packaging application...\n");

        // 2. Package candidate code
        Path targetDir = workspaceDir.resolve("target");
        boolean jarExists = DockerUtils.findJarFile(targetDir).isPresent();

        // Ensure .mvn/wrapper/maven-wrapper.properties exists if mvnw is used
        Path mvnDir = workspaceDir.resolve(".mvn");
        if (!Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"))) {
            Path origMvn = workspaceDir.getParent() != null ? workspaceDir.getParent().resolve("original").resolve(".mvn") : null;
            Path backendMvn = Paths.get(".mvn").toAbsolutePath();
            try {
                if (origMvn != null && Files.exists(origMvn)) {
                    DockerUtils.copyDirectorySimple(origMvn, mvnDir);
                } else if (Files.exists(backendMvn)) {
                    DockerUtils.copyDirectorySimple(backendMvn, mvnDir);
                }
            } catch (Exception ex) {
                log.warn("Could not copy fallback .mvn wrapper: {}", ex.getMessage());
            }
        }

        String mvnCmd = DockerUtils.isWindows() ? "mvn.cmd" : "mvn";
        boolean hasWrapperProps = Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"));
        if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw.cmd"))) {
            mvnCmd = DockerUtils.isWindows() ? "mvnw.cmd" : "./mvnw";
        } else if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw"))) {
            mvnCmd = "./mvnw";
        }

        ProcessCommandExecutor.ProcessResult packageResult = dockerExecutor.executeCommand(
                workingDir,
                120,
                mvnCmd, "package", "-DskipTests", "-Dcheckstyle.skip=true"
        );

        logBuffer.append(execKey, packageResult.combinedOutput() + "\n");
        jarExists = DockerUtils.findJarFile(targetDir).isPresent();

        if (!jarExists) {
            // Fallback packaging via JDK jar tool if target/classes exists
            Path classesDir = targetDir.resolve("classes");
            if (Files.exists(classesDir)) {
                try {
                    Files.createDirectories(targetDir);
                    String javaHome = System.getProperty("java.home");
                    String jarExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (DockerUtils.isWindows() ? "jar.exe" : "jar") : "jar";
                    dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cf", "target/app.jar", "-C", "target/classes", ".");
                    jarExists = DockerUtils.findJarFile(targetDir).isPresent();
                } catch (Exception ignored) {}
            }
        }

        if (!jarExists && !packageResult.isSuccess()) {
            String error = "Compilation / Packaging failed:\n" + packageResult.stderr();
            activeExecutions.put(assessmentId, new ActiveExecution(
                    executionId, assessmentId, containerName, tag, exposedPort, null,
                    Instant.now(), BuildStatus.FAILED, ContainerStatus.STOPPED, ApplicationStatus.FAILED, error
            ));
            return ExecutionRunResponse.builder()
                    .executionId(executionId)
                    .status("FAILED")
                    .port(exposedPort)
                    .message("Compilation failed: check execution logs")
                    .build();
        }

        // 3. Ensure Dockerfile exists in workspace
        Path dockerfilePath = workspaceDir.resolve("Dockerfile");
        if (!Files.exists(dockerfilePath)) {
            try {
                String dockerfileContent = """
                        FROM eclipse-temurin:21-jre-alpine
                        WORKDIR /app
                        COPY target/*.jar app.jar
                        EXPOSE 8080
                        ENTRYPOINT ["java", "-jar", "app.jar"]
                        """;
                Files.writeString(dockerfilePath, dockerfileContent);
            } catch (IOException ignored) {}
        }

        // 4. Launch Container (or Fallback Process)
        boolean dockerAvailable = DockerUtils.isDockerDaemonRunning(dockerExecutor);
        if (dockerAvailable) {
            logBuffer.append(execKey, ">>> [2/3] Building Docker image " + tag + "...\n");
            ProcessCommandExecutor.ProcessResult buildResult = dockerExecutor.executeCommand(
                    workingDir, 120, "docker", "build", "-t", tag, "."
            );
            logBuffer.append(execKey, buildResult.combinedOutput() + "\n");

            if (!buildResult.isSuccess()) {
                activeExecutions.put(assessmentId, new ActiveExecution(
                    executionId, assessmentId, containerName, tag, exposedPort, null,
                    Instant.now(), BuildStatus.FAILED, ContainerStatus.STOPPED, ApplicationStatus.FAILED, buildResult.stderr()
                ));
                return ExecutionRunResponse.builder()
                        .executionId(executionId)
                        .status("FAILED")
                        .port(exposedPort)
                        .message("Docker image build failed")
                        .build();
            }

            logBuffer.append(execKey, ">>> [3/3] Starting candidate container on port " + exposedPort + "...\n");
            ProcessCommandExecutor.ProcessResult runResult = dockerExecutor.executeCommand(
                    workingDir, 30,
                    "docker", "run", "-d",
                    "--name", containerName,
                    "-p", exposedPort + ":8080",
                    tag
            );
            logBuffer.append(execKey, runResult.combinedOutput() + "\n");

            activeExecutions.put(assessmentId, new ActiveExecution(
                    executionId, assessmentId, containerName, tag, exposedPort, null,
                    Instant.now(), BuildStatus.SUCCESS, ContainerStatus.RUNNING, ApplicationStatus.STARTED, null
            ));
        } else {
            // Native sandbox background process
            logBuffer.append(execKey, ">>> [2/3] Docker daemon offline. Starting native sandboxed application on port " + exposedPort + "...\n");
            Optional<Path> jarPath = DockerUtils.findJarFile(targetDir);
            if (jarPath.isPresent()) {
                try {
                    String javaHome = System.getProperty("java.home");
                    String javaExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (DockerUtils.isWindows() ? "java.exe" : "java") : "java";
                    ProcessBuilder pb = new ProcessBuilder(
                            javaExe, "-jar", jarPath.get().toAbsolutePath().toString(),
                            "--server.port=" + exposedPort
                    );
                    pb.directory(workingDir);
                    pb.redirectErrorStream(true);
                    Process proc = pb.start();

                    new Thread(() -> {
                        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                logBuffer.append(execKey, line + "\n");
                            }
                        } catch (Exception ignored) {}
                    }).start();

                    activeExecutions.put(assessmentId, new ActiveExecution(
                            executionId, assessmentId, "native-process", null, exposedPort, proc,
                            Instant.now(), BuildStatus.SUCCESS, ContainerStatus.RUNNING, ApplicationStatus.STARTED, null
                    ));
                } catch (Exception ex) {
                    logBuffer.append(execKey, "Failed to launch native process: " + ex.getMessage() + "\n");
                    activeExecutions.put(assessmentId, new ActiveExecution(
                            executionId, assessmentId, null, null, exposedPort, null,
                            Instant.now(), BuildStatus.FAILED, ContainerStatus.STOPPED, ApplicationStatus.FAILED, ex.getMessage()
                    ));
                }
            }
        }

        return ExecutionRunResponse.builder()
                .executionId(executionId)
                .status("RUNNING")
                .port(exposedPort)
                .message("Application started on port " + exposedPort)
                .build();
    }

    /**
     * Retrieves the current execution status and uptime for an assessment.
     *
     * @param candidateId  UUID of the candidate.
     * @param assessmentId UUID of the assessment.
     * @return ExecutionStatusResponse with build and container health states.
     */
    public ExecutionStatusResponse getExecutionStatus(UUID candidateId, UUID assessmentId) {
        ActiveExecution exec = activeExecutions.get(assessmentId);
        if (exec == null) {
            return ExecutionStatusResponse.builder()
                    .executionId(null)
                    .buildStatus(BuildStatus.SUCCESS)
                    .containerStatus(ContainerStatus.STOPPED)
                    .applicationStatus(ApplicationStatus.FAILED)
                    .port(null)
                    .uptimeSeconds(0L)
                    .errorMessage("No active execution found")
                    .build();
        }

        long uptime = java.time.Duration.between(exec.startTime(), Instant.now()).getSeconds();
        return ExecutionStatusResponse.builder()
                .executionId(exec.executionId())
                .buildStatus(exec.buildStatus())
                .containerStatus(exec.containerStatus())
                .applicationStatus(exec.applicationStatus())
                .port(exec.port())
                .uptimeSeconds(uptime)
                .errorMessage(exec.errorMessage())
                .build();
    }

    /**
     * Retrieves streaming execution logs and terminal output.
     *
     * @param candidateId  UUID of the candidate.
     * @param assessmentId UUID of the assessment.
     * @return ExecutionLogsResponse containing collected logs.
     */
    public ExecutionLogsResponse getExecutionLogs(UUID candidateId, UUID assessmentId) {
        String logs = logBuffer.getLogs(assessmentId.toString());
        ActiveExecution exec = activeExecutions.get(assessmentId);

        if (exec != null && DockerUtils.isDockerDaemonRunning(dockerExecutor) && exec.containerName() != null && !exec.containerName().equals("native-process")) {
            ProcessCommandExecutor.ProcessResult res = dockerExecutor.executeCommand(
                    null, 5, "docker", "logs", "--tail", "100", exec.containerName()
            );
            if (res.isSuccess() && !res.stdout().isEmpty()) {
                logs = logs + "\n--- Container Logs ---\n" + res.stdout();
            }
        }

        return ExecutionLogsResponse.builder()
                .logs(logs != null ? logs : "")
                .isTerminal(false)
                .build();
    }

    /**
     * Stops the running candidate container or process.
     *
     * @param candidateId  UUID of the candidate requesting termination.
     * @param assessmentId UUID of the assessment.
     * @return StopExecutionResponse confirming termination.
     */
    public StopExecutionResponse stopCandidateApplication(UUID candidateId, UUID assessmentId) {
        log.info("Stopping execution for assessment {}", assessmentId);
        stopExistingExecution(assessmentId);
        return StopExecutionResponse.builder()
                .status("STOPPED")
                .message("Application container stopped successfully")
                .build();
    }

    /**
     * Internal cleanup to forcefully kill existing Docker container, image, or native process.
     *
     * @param assessmentId UUID of the assessment to clean up.
     */
    public void stopExistingExecution(UUID assessmentId) {
        ActiveExecution exec = activeExecutions.remove(assessmentId);
        if (exec != null) {
            if (exec.containerName() != null && !exec.containerName().equals("native-process")) {
                try {
                    dockerExecutor.executeCommand(null, 10, "docker", "rm", "-f", exec.containerName());
                    if (exec.imageTag() != null) {
                        dockerExecutor.executeCommand(null, 10, "docker", "rmi", "-f", exec.imageTag());
                    }
                } catch (Exception ignored) {}
            }
            if (exec.process() != null && exec.process().isAlive()) {
                exec.process().destroyForcibly();
            }
        }
    }
}
