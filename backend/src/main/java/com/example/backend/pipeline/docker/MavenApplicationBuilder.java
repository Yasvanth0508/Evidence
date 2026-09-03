package com.example.backend.pipeline.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class MavenApplicationBuilder implements ApplicationBuilder {

    private static final Logger log = LoggerFactory.getLogger(MavenApplicationBuilder.class);
    private final ProcessCommandExecutor dockerExecutor;

    public MavenApplicationBuilder(ProcessCommandExecutor dockerExecutor) {
        this.dockerExecutor = dockerExecutor;
    }

    @Override
    public BuildResult buildApplication(UUID assessmentId, Path targetBackendDir) {
        File workingDir = targetBackendDir.toFile();
        long buildStartTime = System.currentTimeMillis();

        Path targetDir = targetBackendDir.resolve("target");
        boolean jarExists = findJarFile(targetDir).isPresent();
        StringBuilder buildLogs = new StringBuilder();

        if (!jarExists) {
            String mvnCmd = isWindows() ? "mvn.cmd" : "mvn";
            File wrapperJar = new File(workingDir, ".mvn" + File.separator + "wrapper" + File.separator + "maven-wrapper.jar");
            if (new File(workingDir, isWindows() ? "mvnw.cmd" : "mvnw").exists() && wrapperJar.exists() && wrapperJar.length() > 5000) {
                mvnCmd = new File(workingDir, isWindows() ? "mvnw.cmd" : "mvnw").getAbsolutePath();
            }

            ProcessCommandExecutor.ProcessResult packageResult = dockerExecutor.executeCommand(
                    workingDir,
                    180,
                    mvnCmd, "package", "-DskipTests", "-Dcheckstyle.skip=true"
            );
            buildLogs.append(packageResult.combinedOutput());

            jarExists = findJarFile(targetDir).isPresent();

            if (!jarExists) {
                Path classesDir = targetDir.resolve("classes");
                if (Files.exists(classesDir)) {
                    log.info("Packaging compiled classes from {} into target/app.jar", classesDir);
                    try {
                        Files.createDirectories(targetDir);
                        String javaHome = System.getProperty("java.home");
                        String jarExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (isWindows() ? "jar.exe" : "jar") : "jar";
                        String mainClass = findMainClassName(targetBackendDir);
                        if (mainClass != null && !mainClass.isEmpty()) {
                            dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cfe", "target/app.jar", mainClass, "-C", "target/classes", ".");
                        } else {
                            dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cf", "target/app.jar", "-C", "target/classes", ".");
                        }
                        jarExists = findJarFile(targetDir).isPresent();
                    } catch (Exception ex) {
                        log.warn("Jar packaging fallback exception: {}", ex.getMessage());
                    }
                }
            }

            if (!jarExists && !packageResult.isSuccess()) {
                log.error("Phase 2: Project compilation/package failed: {}", packageResult.combinedOutput());
                return new BuildResult(false, System.currentTimeMillis() - buildStartTime, packageResult.combinedOutput(), "Project packaging failed: " + packageResult.stderr());
            }
        }

        Path dockerfilePath = targetBackendDir.resolve("Dockerfile");
        if (!Files.exists(dockerfilePath)) {
            try {
                generateStandardDockerfile(dockerfilePath, targetDir);
                log.info("Generated standard Spring Boot Dockerfile in {}", dockerfilePath);
            } catch (IOException e) {
                return new BuildResult(false, System.currentTimeMillis() - buildStartTime, buildLogs.toString(), "Could not create Dockerfile: " + e.getMessage());
            }
        }

        long buildDurationMs = System.currentTimeMillis() - buildStartTime;
        return new BuildResult(true, buildDurationMs, buildLogs.toString(), null);
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
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private void generateStandardDockerfile(Path dockerfilePath, Path targetDir) throws IOException {
        String jarName = "app.jar";
        try (Stream<Path> stream = Files.list(targetDir)) {
            Optional<Path> foundJar = stream.filter(p -> p.getFileName().toString().endsWith(".jar") && !p.getFileName().toString().startsWith("original-")).findFirst();
            if (foundJar.isPresent()) {
                jarName = foundJar.get().getFileName().toString();
            }
        } catch (Exception ignored) {
        }

        String dockerfileContent = String.format("""
                FROM eclipse-temurin:21-jre-alpine
                WORKDIR /app
                COPY target/%s app.jar
                EXPOSE 8080
                ENTRYPOINT ["java", "-jar", "app.jar"]
                """, jarName);
        Files.writeString(dockerfilePath, dockerfileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String findMainClassName(Path targetBackendDir) {
        Path srcMainJava = targetBackendDir.resolve("src").resolve("main").resolve("java");
        if (!Files.exists(srcMainJava)) return null;

        try (Stream<Path> stream = Files.walk(srcMainJava)) {
            Optional<Path> mainFile = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            String content = Files.readString(p);
                            return content.contains("@SpringBootApplication") || content.contains("public static void main");
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst();

            if (mainFile.isPresent()) {
                String content = Files.readString(mainFile.get());
                String pkg = "";
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("package\\s+([a-zA-Z0-9_.]+);").matcher(content);
                if (m.find()) {
                    pkg = m.group(1).trim() + ".";
                }
                String className = mainFile.get().getFileName().toString().replace(".java", "");
                return pkg + className;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
