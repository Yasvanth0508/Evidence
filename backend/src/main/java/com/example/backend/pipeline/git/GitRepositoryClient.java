package com.example.backend.pipeline.git;

import java.nio.file.Path;

public interface GitRepositoryClient {
    boolean cloneRepository(String repoUrl, String branch, Path targetDir);
    String extractCommitHash(Path targetDir);
}
