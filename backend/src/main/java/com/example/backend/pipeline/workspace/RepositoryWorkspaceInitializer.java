package com.example.backend.pipeline.workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.backend.pipeline.git.GitCloningService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.UUID;

@Service
public class RepositoryWorkspaceInitializer {

    private static final Logger log = LoggerFactory.getLogger(RepositoryWorkspaceInitializer.class);

    private final GitCloningService gitCloningService;

    private static final Set<String> IGNORED_COPY_NAMES = Set.of(
            ".git", "target", ".idea", "node_modules", ".settings", ".classpath", ".project", ".DS_Store"
    );
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".war", ".nar", ".zip", ".tar", ".gz"
    );

    public RepositoryWorkspaceInitializer(GitCloningService gitCloningService) {
        this.gitCloningService = gitCloningService;
    }

    public void cloneRepositoryIfNeeded(UUID assessmentId, String repoUrl, String branch, Path originalRepoPath) {
        if (!Files.exists(originalRepoPath) && repoUrl != null && !repoUrl.trim().isEmpty()) {
            log.info("Cloning repository on demand for candidate workspace: {}", repoUrl);
            gitCloningService.cloneRepository(assessmentId, repoUrl, branch);
        }
    }

    public void initializeWorkspace(Path originalRepoPath, Path candidateWorkspacePath) throws IOException {
        if (!Files.exists(candidateWorkspacePath)) {
            Files.createDirectories(candidateWorkspacePath);
            if (Files.exists(originalRepoPath)) {
                copyDirectoryTree(originalRepoPath, candidateWorkspacePath);
                createStarterFilesIfMissing(candidateWorkspacePath);
                log.info("Copied original repository from {} to candidate workspace {}", originalRepoPath, candidateWorkspacePath);
            } else {
                createStarterFilesIfMissing(candidateWorkspacePath);
                log.info("Created starter template files in candidate workspace {}", candidateWorkspacePath);
            }
        }
    }

    private boolean shouldIgnoreForCopy(String name) {
        if (IGNORED_COPY_NAMES.contains(name)) return true;
        for (String ext : IGNORED_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private void copyDirectoryTree(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnoreForCopy(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnoreForCopy(file.getFileName().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void createStarterFilesIfMissing(Path candidateWorkspacePath) throws IOException {
        Path srcMainJava = candidateWorkspacePath.resolve("src").resolve("main").resolve("java").resolve("com").resolve("example");
        Files.createDirectories(srcMainJava);

        Path pomXml = candidateWorkspacePath.resolve("pom.xml");
        if (!Files.exists(pomXml)) {
            String defaultPom = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>assessment-app</artifactId>
                        <version>1.0.0</version>
                        <properties>
                            <java.version>21</java.version>
                        </properties>
                    </project>
                    """;
            Files.writeString(pomXml, defaultPom);
        }
    }
}
