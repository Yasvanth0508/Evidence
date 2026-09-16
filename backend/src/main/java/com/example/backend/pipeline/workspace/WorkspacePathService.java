package com.example.backend.pipeline.workspace;

import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class WorkspacePathService {

    private static final String STORAGE_BASE_DIR = "storage/assessments";

    public Path resolveOriginalRepoPath(UUID assessmentId) {
        return Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "original").toAbsolutePath().normalize();
    }

    public Path resolveCandidateWorkspacePath(UUID assessmentId) {
        return Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "candidate_workspace").toAbsolutePath().normalize();
    }

    public Path sanitizeAndResolvePath(Path rootDir, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new BadRequestException("Path must not be empty");
        }
        Path resolved = rootDir.resolve(relativePath.trim()).normalize();
        if (!resolved.startsWith(rootDir)) {
            throw new ForbiddenException("Access outside candidate workspace directory is forbidden");
        }
        return resolved;
    }

    /**
     * Resolves the target backend directory containing the Maven project (pom.xml),
     * supporting monorepos with subdirectories like "backend/".
     */
    public Path resolveBackendDirectory(Path workspaceRoot, String backendRootDirectory) {
        if (workspaceRoot == null || !Files.exists(workspaceRoot)) {
            return workspaceRoot;
        }

        // 1. Explicit backendRootDirectory if configured
        if (backendRootDirectory != null && !backendRootDirectory.trim().isEmpty()) {
            Path candidate = workspaceRoot.resolve(backendRootDirectory.trim()).normalize();
            if (Files.exists(candidate.resolve("pom.xml"))) {
                return candidate;
            }
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        // 2. Standard "backend" subfolder if it contains a pom.xml
        Path backendFolder = workspaceRoot.resolve("backend");
        if (Files.exists(backendFolder.resolve("pom.xml"))) {
            return backendFolder;
        }

        // 3. Root directory if it has pom.xml and is not an accidental dummy pom
        Path rootPom = workspaceRoot.resolve("pom.xml");
        if (Files.exists(rootPom)) {
            try {
                String content = Files.readString(rootPom);
                // If it's not our accidental starter POM or no other pom exists, use root
                if (!content.contains("<artifactId>assessment-app</artifactId>") || !hasSubdirectoryPom(workspaceRoot)) {
                    return workspaceRoot;
                }
            } catch (Exception ignored) {
                return workspaceRoot;
            }
        }

        // 4. Auto-detect any subdirectory containing pom.xml
        try (Stream<Path> stream = Files.walk(workspaceRoot, 2)) {
            Path found = stream
                    .filter(p -> p.getFileName().toString().equals("pom.xml") && !p.getParent().equals(workspaceRoot))
                    .map(Path::getParent)
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                return found;
            }
        } catch (IOException ignored) {}

        return workspaceRoot;
    }

    private boolean hasSubdirectoryPom(Path rootDir) {
        try (Stream<Path> stream = Files.walk(rootDir, 2)) {
            return stream.anyMatch(p -> p.getFileName().toString().equals("pom.xml") && !p.getParent().equals(rootDir));
        } catch (IOException e) {
            return false;
        }
    }
}
