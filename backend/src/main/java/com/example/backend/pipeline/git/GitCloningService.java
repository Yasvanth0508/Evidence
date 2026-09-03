package com.example.backend.pipeline.git;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.AssessmentWorkspace;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.AssessmentWorkspaceRepository;
import com.example.backend.pipeline.git.dto.GitCloneResult;
import com.example.backend.pipeline.storage.RepositoryStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GitCloningService {

    private static final Logger log = LoggerFactory.getLogger(GitCloningService.class);

    private final AssessmentWorkspaceRepository workspaceRepository;
    private final AssessmentRepository assessmentRepository;
    private final GitRepositoryClient gitClient;
    private final RepositoryStorageService storageService;

    public GitCloningService(AssessmentWorkspaceRepository workspaceRepository,
                             AssessmentRepository assessmentRepository,
                             GitRepositoryClient gitClient,
                             RepositoryStorageService storageService) {
        this.workspaceRepository = workspaceRepository;
        this.assessmentRepository = assessmentRepository;
        this.gitClient = gitClient;
        this.storageService = storageService;
    }

    @Transactional
    public GitCloneResult cloneRepository(UUID assessmentId, String repositoryUrl, String branchName) {
        log.info("Phase 1: Starting Git Clone for Assessment ID: {} | URL: {} | Branch: {}",
                assessmentId, repositoryUrl, branchName);

        Path targetDir = storageService.resolveAssessmentOriginalPath(assessmentId);

        try {
            storageService.deleteDirectoryRecursively(targetDir);
            storageService.createDirectories(targetDir.getParent());

            String branchToClone = (branchName != null && !branchName.trim().isEmpty()) ? branchName.trim() : "main";

            boolean cloned = gitClient.cloneRepository(repositoryUrl.trim(), branchToClone, targetDir);

            if (!cloned) {
                throw new IllegalStateException("Failed to clone repository from URL: " + repositoryUrl);
            }

            String commitHash = gitClient.extractCommitHash(targetDir);

            int totalFiles = countFiles(targetDir);
            long totalSize = calculateDirectorySize(targetDir);
            List<String> topLevelFiles = listTopLevelFiles(targetDir);

            saveAssessmentWorkspacePath(assessmentId, targetDir.toAbsolutePath().toString());

            log.info("Phase 1: Successfully cloned repository to {} (Files: {}, Size: {} bytes, Commit: {})",
                    targetDir.toAbsolutePath(), totalFiles, totalSize, commitHash);

            return GitCloneResult.ok(
                    targetDir.toAbsolutePath().toString(),
                    branchToClone,
                    commitHash,
                    totalFiles,
                    totalSize,
                    topLevelFiles
            );

        } catch (Exception ex) {
            log.error("Phase 1: Git clone failed for assessment {}: {}", assessmentId, ex.getMessage(), ex);
            return GitCloneResult.fail("Git clone failed: " + ex.getMessage());
        }
    }

    private void saveAssessmentWorkspacePath(UUID assessmentId, String originalRepoPath) {
        if (workspaceRepository == null || assessmentId == null) {
            return;
        }

        try {
            AssessmentWorkspace workspace = workspaceRepository.findById(assessmentId).orElse(null);
            if (workspace == null && assessmentRepository != null) {
                Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
                if (assessment != null) {
                    workspace = new AssessmentWorkspace(assessment, originalRepoPath, null);
                }
            }

            if (workspace != null) {
                workspace.setOriginalRepositoryPath(originalRepoPath);
                workspaceRepository.save(workspace);
                log.info("Phase 1: Saved original_repository_path to ASSESSMENT_WORKSPACE for assessment {}", assessmentId);
            }
        } catch (Exception ex) {
            log.warn("Could not save ASSESSMENT_WORKSPACE record: {}", ex.getMessage());
        }
    }

    private int countFiles(Path dir) {
        try (Stream<Path> stream = Files.walk(dir)) {
            return (int) stream.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private long calculateDirectorySize(Path dir) {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    }).sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    private List<String> listTopLevelFiles(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
