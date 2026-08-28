package com.example.backend.pipeline.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DockerCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(DockerCommandExecutor.class);

    public record ProcessResult(int exitCode, String stdout, String stderr, long durationMs) {
        public boolean isSuccess() {
            return exitCode == 0;
        }

        public String combinedOutput() {
            if (stderr == null || stderr.isEmpty()) return stdout;
            if (stdout == null || stdout.isEmpty()) return stderr;
            return stdout + "\n" + stderr;
        }
    }

    public ProcessResult executeCommand(File workingDir, long timeoutSeconds, String... command) {
        log.debug("Executing Docker/Process command: {} in directory {}", Arrays.toString(command), workingDir);
        long startTime = System.currentTimeMillis();

        try {
            List<String> commandList = new ArrayList<>(Arrays.asList(command));
            boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");

            // On Windows, if command starts with a script, mvn/mvnw, or docker, invoke through cmd.exe /c
            if (isWindows && commandList.size() > 0) {
                String first = commandList.get(0);
                if (first.endsWith(".cmd") || first.endsWith(".bat") || first.equals("mvn") || first.equals("mvnw") || first.equals("docker") || first.equals("docker.exe")) {
                    List<String> wrapped = new ArrayList<>();
                    wrapped.add("cmd.exe");
                    wrapped.add("/c");
                    wrapped.addAll(commandList);
                    commandList = wrapped;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commandList);
            Map<String, String> env = pb.environment();
            env.put("MAVEN_OPTS", "-Xmx256m -Xms64m -XX:+UseSerialGC");

            // Ensure JAVA_HOME and Path are explicitly set for process execution
            String javaHome = System.getProperty("java.home");
            if (javaHome != null && !javaHome.trim().isEmpty()) {
                env.put("JAVA_HOME", javaHome.trim());
                String currentPath = env.get("Path");
                if (currentPath == null) currentPath = env.get("PATH");
                String javaBin = javaHome.trim() + File.separator + "bin";
                if (currentPath != null && !currentPath.contains(javaBin)) {
                    env.put("Path", javaBin + File.pathSeparator + currentPath);
                } else if (currentPath == null) {
                    env.put("Path", javaBin);
                }
            }

            if (workingDir != null && workingDir.exists()) {
                pb.directory(workingDir);
            }

            Process process = pb.start();

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread outThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });

            Thread errThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });

            outThread.start();
            errThread.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                outThread.interrupt();
                errThread.interrupt();
                return new ProcessResult(-1, stdout.toString(), "Command timed out after " + timeoutSeconds + " seconds", duration);
            }

            outThread.join(2000);
            errThread.join(2000);

            return new ProcessResult(process.exitValue(), stdout.toString().trim(), stderr.toString().trim(), duration);

        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to execute process command {}: {}", Arrays.toString(command), ex.getMessage());
            return new ProcessResult(-1, "", "Execution error: " + ex.getMessage(), duration);
        }
    }
}
