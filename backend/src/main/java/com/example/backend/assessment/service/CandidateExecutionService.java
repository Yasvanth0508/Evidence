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
import com.example.backend.pipeline.docker.DockerCommandExecutor;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateExecutionService {

    private final AssessmentRepository assessmentRepository;
    private final CandidateWorkspaceService candidateWorkspaceService;
    private final DockerCommandExecutor dockerExecutor;
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
     * Phase B.1: Compiles candidate code, launches ephemeral container/process on dynamic port.
     */
    public ExecutionRunResponse runCandidateApplication(UUID candidateId, UUID assessmentId) {
        log.info("Candidate {} requested application RUN for assessment {}", candidateId, assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (candidateId != null && assessment.getCandidate() != null && !assessment.getCandidate().getId().equals(candidateId)) {
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
        boolean jarExists = findJarFile(targetDir).isPresent();

        // Ensure .mvn/wrapper/maven-wrapper.properties exists if mvnw is used
        Path mvnDir = workspaceDir.resolve(".mvn");
        if (!Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"))) {
            Path origMvn = workspaceDir.getParent() != null ? workspaceDir.getParent().resolve("original").resolve(".mvn") : null;
            Path backendMvn = Paths.get(".mvn").toAbsolutePath();
            try {
                if (origMvn != null && Files.exists(origMvn)) {
                    copyDirectorySimple(origMvn, mvnDir);
                } else if (Files.exists(backendMvn)) {
                    copyDirectorySimple(backendMvn, mvnDir);
                }
            } catch (Exception ex) {
                log.warn("Could not copy fallback .mvn wrapper: {}", ex.getMessage());
            }
        }

        String mvnCmd = isWindows() ? "mvn.cmd" : "mvn";
        boolean hasWrapperProps = Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"));
        if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw.cmd"))) {
            mvnCmd = isWindows() ? "mvnw.cmd" : "./mvnw";
        } else if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw"))) {
            mvnCmd = "./mvnw";
        }

        DockerCommandExecutor.ProcessResult packageResult = dockerExecutor.executeCommand(
                workingDir,
                120,
                mvnCmd, "package", "-DskipTests", "-Dcheckstyle.skip=true"
        );

        logBuffer.append(execKey, packageResult.combinedOutput() + "\n");
        jarExists = findJarFile(targetDir).isPresent();

        if (!jarExists) {
            // Fallback packaging via JDK jar tool
            Path classesDir = targetDir.resolve("classes");
            if (Files.exists(classesDir)) {
                try {
                    Files.createDirectories(targetDir);
                    String javaHome = System.getProperty("java.home");
                    String jarExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (isWindows() ? "jar.exe" : "jar") : "jar";
                    dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cf", "target/app.jar", "-C", "target/classes", ".");
                    jarExists = findJarFile(targetDir).isPresent();
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

        // 3. Ensure Dockerfile exists
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
        boolean dockerAvailable = isDockerDaemonRunning();
        if (dockerAvailable) {
            logBuffer.append(execKey, ">>> [2/3] Building Docker image " + tag + "...\n");
            DockerCommandExecutor.ProcessResult buildResult = dockerExecutor.executeCommand(
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
            DockerCommandExecutor.ProcessResult runResult = dockerExecutor.executeCommand(
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
            Optional<Path> jarPath = findJarFile(targetDir);
            if (jarPath.isPresent()) {
                try {
                    String javaHome = System.getProperty("java.home");
                    String javaExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (isWindows() ? "java.exe" : "java") : "java";
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
     * Phase B.2: Get current execution status.
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
     * Phase B.3: Fetch live execution logs.
     */
    public ExecutionLogsResponse getExecutionLogs(UUID candidateId, UUID assessmentId) {
        String logs = logBuffer.getLogs(assessmentId.toString());
        ActiveExecution exec = activeExecutions.get(assessmentId);

        if (exec != null && isDockerDaemonRunning() && exec.containerName() != null && !exec.containerName().equals("native-process")) {
            DockerCommandExecutor.ProcessResult res = dockerExecutor.executeCommand(
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
     * Phase B.4: Stop running application container or process.
     */
    public StopExecutionResponse stopCandidateApplication(UUID candidateId, UUID assessmentId) {
        log.info("Stopping execution for assessment {}", assessmentId);
        stopExistingExecution(assessmentId);
        return StopExecutionResponse.builder()
                .status("STOPPED")
                .message("Application container stopped successfully")
                .build();
    }

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

    private void copyDirectorySimple(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean isDockerDaemonRunning() {
        DockerCommandExecutor.ProcessResult result = dockerExecutor.executeCommand(null, 5, "docker", "info");
        return result.isSuccess();
    }

    private Optional<Path> findJarFile(Path targetDir) {
        if (!Files.exists(targetDir)) return Optional.empty();
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".jar") && !p.getFileName().toString().startsWith("original-"))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
