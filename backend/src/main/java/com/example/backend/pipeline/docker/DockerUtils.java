package com.example.backend.pipeline.docker;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Shared utility class for workspace filesystem operations, artifact discovery,
 * and Docker environment checks.
 */
@Slf4j
public final class DockerUtils {

    private DockerUtils() {
        // Utility class private constructor
    }

    /**
     * Recursively copies a directory tree from source to destination.
     *
     * @param source      The source directory path.
     * @param destination The destination directory path.
     * @throws IOException if any I/O error occurs during directory traversal or file copy.
     */
    public static void copyDirectorySimple(Path source, Path destination) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static boolean isDockerDaemonRunning(DockerCommandExecutor dockerExecutor) {
        if (dockerExecutor == null) {
            return false;
        }
        DockerCommandExecutor.ProcessResult result = dockerExecutor.executeCommand(
                null, 10, "docker", "version", "--format", "{{.Server.Version}}"
        );
        if (result.isSuccess() && !result.stdout().isBlank()) {
            return true;
        }
        // Fallback check with docker info
        DockerCommandExecutor.ProcessResult fallback = dockerExecutor.executeCommand(null, 10, "docker", "info");
        return fallback.isSuccess();
    }

    /**
     * Searches a target directory for an executable or runnable Spring Boot fat JAR.
     *
     * @param targetDir The directory to inspect (e.g. workspace target folder).
     * @return Optional containing the Path to the found .jar file, or Optional.empty().
     */
    public static Optional<Path> findJarFile(Path targetDir) {
        if (targetDir == null || !Files.exists(targetDir)) {
            return Optional.empty();
        }
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".jar")
                            && !p.getFileName().toString().startsWith("original-")
                            && !p.getFileName().toString().endsWith("-sources.jar")
                            && !p.getFileName().toString().endsWith("-javadoc.jar"))
                    .findFirst();
        } catch (IOException e) {
            log.warn("Error listing target directory {}: {}", targetDir, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Checks whether the current operating system is Windows.
     *
     * @return true if Windows OS, false otherwise.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
