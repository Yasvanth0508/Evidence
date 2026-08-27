package com.example.backend.pipeline.git;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.AssessmentWorkspace;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.AssessmentWorkspaceRepository;
import com.example.backend.pipeline.git.dto.GitCloneResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GitCloningService {

    private static final Logger log = LoggerFactory.getLogger(GitCloningService.class);

    private final AssessmentWorkspaceRepository workspaceRepository;
    private final AssessmentRepository assessmentRepository;

    public GitCloningService(AssessmentWorkspaceRepository workspaceRepository,
                             AssessmentRepository assessmentRepository) {
        this.workspaceRepository = workspaceRepository;
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Executes Phase 1: Clones live target repository directly from GitHub to storage/assessments/{assessmentId}/original
     */
    @Transactional
    public GitCloneResult cloneRepository(UUID assessmentId, String repositoryUrl, String branchName) {
        log.info("Phase 1: Starting Git Clone for Assessment ID: {} | URL: {} | Branch: {}",
                assessmentId, repositoryUrl, branchName);

        Path targetDir = resolveAssessmentOriginalPath(assessmentId);

        try {
            // 1. Clean previous directory if exists
            deleteDirectoryRecursively(targetDir);
            Files.createDirectories(targetDir.getParent());

            String branchToClone = (branchName != null && !branchName.trim().isEmpty()) ? branchName.trim() : "main";
            String commitHash = "UNKNOWN";

            // 2. Perform live Git Clone using native Git CLI / JGit
            boolean cloned = cloneWithNativeGit(repositoryUrl.trim(), branchToClone, targetDir);
            if (!cloned) {
                log.info("Native git clone was not successful, trying Eclipse JGit clone...");
                cloned = cloneWithJGit(repositoryUrl.trim(), branchToClone, targetDir);
            }

            if (!cloned) {
                throw new IllegalStateException("Failed to clone repository from URL: " + repositoryUrl);
            }

            // 3. Extract Real Commit Hash
            commitHash = extractCommitHash(targetDir);

            // 4. Compute real file stats
            int totalFiles = countFiles(targetDir);
            long totalSize = calculateDirectorySize(targetDir);
            List<String> topLevelFiles = listTopLevelFiles(targetDir);

            // 5. Persist AssessmentWorkspace in PostgreSQL
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

    private boolean cloneWithNativeGit(String repoUrl, String branch, Path targetDir) {
        try {
            List<String> command = List.of(
                    "git", "clone",
                    "--branch", branch,
                    "--depth", "1",
                    repoUrl,
                    targetDir.toAbsolutePath().toString()
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                log.info("Native Git clone completed successfully.");
                return true;
            } else {
                log.warn("Native Git clone returned exit code {}: {}", process.exitValue(), output);
                return false;
            }
        } catch (Exception e) {
            log.warn("Native Git clone exception: {}", e.getMessage());
            return false;
        }
    }

    private boolean cloneWithJGit(String repoUrl, String branch, Path targetDir) {
        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(targetDir.toFile())
                .setBranch(branch)
                .call()) {
            return true;
        } catch (Exception ex) {
            log.warn("JGit clone exception: {}", ex.getMessage());
            return false;
        }
    }

    private String extractCommitHash(Path targetDir) {
        // Try native git rev-parse HEAD
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
            pb.directory(targetDir.toFile());
            Process p = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = r.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // Try JGit repo resolve
        try (Git git = Git.open(targetDir.toFile())) {
            Repository repo = git.getRepository();
            ObjectId head = repo.resolve("HEAD");
            if (head != null) {
                return head.getName();
            }
        } catch (Exception ignored) {
        }

        return "UNKNOWN";
    }

    public Path resolveAssessmentOriginalPath(UUID assessmentId) {
        return Paths.get(System.getProperty("user.dir"), "storage", "assessments", assessmentId.toString(), "original");
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

    public void deleteDirectoryRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        file.toFile().setWritable(true);
                        Files.deleteIfExists(file);
                    } catch (Exception ignored) {
                        file.toFile().deleteOnExit();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exc) {
                    try {
                        directory.toFile().setWritable(true);
                        Files.deleteIfExists(directory);
                    } catch (Exception ignored) {
                        directory.toFile().deleteOnExit();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            log.warn("Directory cleanup notice for {}: {}", dir, ex.getMessage());
        }
    }
}
