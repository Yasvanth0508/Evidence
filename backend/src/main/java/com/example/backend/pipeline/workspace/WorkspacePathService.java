package com.example.backend.pipeline.workspace;

import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
}
