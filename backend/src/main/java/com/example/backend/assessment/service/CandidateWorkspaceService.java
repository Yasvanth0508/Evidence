package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.workspace.*;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.AssessmentWorkspace;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.AssessmentWorkspaceRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.AssessmentNotAvailableException;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateWorkspaceService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentWorkspaceRepository assessmentWorkspaceRepository;
    private final com.example.backend.pipeline.git.GitCloningService gitCloningService;

    private static final String STORAGE_BASE_DIR = "storage/assessments";
    private static final Set<String> IGNORED_UI_NAMES = Set.of(
            ".git", "target", ".idea", "node_modules", ".mvn", ".settings", ".classpath", ".project", ".DS_Store"
    );
    private static final Set<String> IGNORED_COPY_NAMES = Set.of(
            ".git", "target", ".idea", "node_modules", ".settings", ".classpath", ".project", ".DS_Store"
    );
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".war", ".nar", ".zip", ".tar", ".gz"
    );

    /**
     * Phase A.1: Start assessment, copy original repo to candidate workspace, update status to IN_PROGRESS.
     */
    @Transactional
    public StartAssessmentResponse startAssessment(UUID candidateId, UUID assessmentId) {
        log.info("Candidate {} is starting Assessment {}", candidateId, assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (candidateId != null && assessment.getCandidate() != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Instant now = Instant.now();
        if (assessment.getScheduledStartAt() != null && now.isBefore(assessment.getScheduledStartAt().minusSeconds(300))) {
            throw new AssessmentNotAvailableException("Assessment is not yet open. Scheduled start: " + assessment.getScheduledStartAt());
        }
        if (assessment.getScheduledEndAt() != null && now.isAfter(assessment.getScheduledEndAt())) {
            throw new AssessmentNotAvailableException("Assessment has already ended. Scheduled end: " + assessment.getScheduledEndAt());
        }

        Path originalRepoPath = Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "original").toAbsolutePath().normalize();
        Path candidateWorkspacePath = Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "candidate_workspace").toAbsolutePath().normalize();

        try {
            if (!Files.exists(originalRepoPath) && assessment.getRepositoryUrl() != null && !assessment.getRepositoryUrl().trim().isEmpty()) {
                log.info("Cloning repository on demand for candidate workspace: {}", assessment.getRepositoryUrl());
                gitCloningService.cloneRepository(assessmentId, assessment.getRepositoryUrl(), assessment.getBranchName());
            }

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
        } catch (Exception e) {
            log.error("Failed to initialize candidate workspace directory for assessment {}", assessmentId, e);
            throw new RuntimeException("Could not initialize candidate workspace directory", e);
        }

        AssessmentWorkspace workspace = assessmentWorkspaceRepository.findByAssessmentId(assessmentId)
                .orElseGet(() -> new AssessmentWorkspace(assessment, originalRepoPath.toString(), candidateWorkspacePath.toString()));

        workspace.setOriginalRepositoryPath(originalRepoPath.toString());
        workspace.setCandidateWorkspacePath(candidateWorkspacePath.toString());
        assessmentWorkspaceRepository.save(workspace);

        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        assessmentRepository.save(assessment);

        return StartAssessmentResponse.builder()
                .assessmentId(assessmentId)
                .status(AssessmentStatus.IN_PROGRESS)
                .workspacePath(candidateWorkspacePath.toString())
                .startedAt(now)
                .build();
    }

    /**
     * Phase A.2: Recursively fetch hierarchical file explorer directory tree.
     */
    @Transactional(readOnly = true)
    public FileNodeDto getFileTree(UUID candidateId, UUID assessmentId) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        return buildFileNode(workspaceDir.toFile(), workspaceDir);
    }

    /**
     * Phase A.3: Read file content with path traversal sanitization.
     */
    @Transactional(readOnly = true)
    public FileContentResponse getFileContent(UUID candidateId, UUID assessmentId, String relativePath) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path targetFile = sanitizeAndResolvePath(workspaceDir, relativePath);

        if (!Files.exists(targetFile) || Files.isDirectory(targetFile)) {
            throw new ResourceNotFoundException("File not found: " + relativePath);
        }

        try {
            String content = Files.readString(targetFile);
            long size = Files.size(targetFile);
            String language = detectLanguage(targetFile.getFileName().toString());

            return FileContentResponse.builder()
                    .path(relativePath.replace("\\", "/"))
                    .content(content)
                    .language(language)
                    .sizeBytes(size)
                    .build();
        } catch (IOException e) {
            log.error("Failed to read file content at {}", targetFile, e);
            throw new RuntimeException("Error reading file: " + relativePath, e);
        }
    }

    /**
     * Phase A.4: Save / debounced autosave file content.
     */
    @Transactional(readOnly = true)
    public void saveFileContent(UUID candidateId, UUID assessmentId, SaveFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path targetFile = sanitizeAndResolvePath(workspaceDir, request.getPath());

        try {
            if (targetFile.getParent() != null && !Files.exists(targetFile.getParent())) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.writeString(targetFile, request.getContent(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            log.debug("Saved file content to {}", targetFile);
        } catch (IOException e) {
            log.error("Failed to save file content at {}", targetFile, e);
            throw new RuntimeException("Could not save file: " + request.getPath(), e);
        }
    }

    /**
     * Phase A.5: Create new file or directory.
     */
    @Transactional(readOnly = true)
    public void createFileOrDirectory(UUID candidateId, UUID assessmentId, CreateFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path target = sanitizeAndResolvePath(workspaceDir, request.getPath());

        try {
            if ("DIRECTORY".equalsIgnoreCase(request.getType())) {
                Files.createDirectories(target);
                log.info("Created directory in candidate workspace: {}", target);
            } else {
                if (target.getParent() != null && !Files.exists(target.getParent())) {
                    Files.createDirectories(target.getParent());
                }
                String content = request.getInitialContent() != null ? request.getInitialContent() : "";
                Files.writeString(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                log.info("Created file in candidate workspace: {}", target);
            }
        } catch (IOException e) {
            log.error("Failed to create file/directory at {}", target, e);
            throw new RuntimeException("Could not create " + request.getPath(), e);
        }
    }

    /**
     * Phase A.5: Delete file or directory.
     */
    @Transactional(readOnly = true)
    public void deleteFileOrDirectory(UUID candidateId, UUID assessmentId, String relativePath) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path target = sanitizeAndResolvePath(workspaceDir, relativePath);

        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("File or directory not found: " + relativePath);
        }

        try {
            if (Files.isDirectory(target)) {
                deleteRecursively(target);
                log.info("Deleted directory recursively in candidate workspace: {}", target);
            } else {
                Files.deleteIfExists(target);
                log.info("Deleted file in candidate workspace: {}", target);
            }
        } catch (IOException e) {
            log.error("Failed to delete file/directory at {}", target, e);
            throw new RuntimeException("Could not delete " + relativePath, e);
        }
    }

    /**
     * Phase A.5: Rename or move file / directory.
     */
    @Transactional(readOnly = true)
    public void renameFileOrDirectory(UUID candidateId, UUID assessmentId, RenameFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path oldTarget = sanitizeAndResolvePath(workspaceDir, request.getOldPath());
        Path newTarget = sanitizeAndResolvePath(workspaceDir, request.getNewPath());

        if (!Files.exists(oldTarget)) {
            throw new ResourceNotFoundException("Source file not found: " + request.getOldPath());
        }

        try {
            if (newTarget.getParent() != null && !Files.exists(newTarget.getParent())) {
                Files.createDirectories(newTarget.getParent());
            }
            Files.move(oldTarget, newTarget, StandardCopyOption.REPLACE_EXISTING);
            log.info("Renamed {} to {}", oldTarget, newTarget);
        } catch (IOException e) {
            log.error("Failed to rename {} to {}", oldTarget, newTarget, e);
            throw new RuntimeException("Could not rename file", e);
        }
    }

    public Path resolveCandidateWorkspace(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (candidateId != null && assessment.getCandidate() != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Path candidateWorkspacePath = Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "candidate_workspace").toAbsolutePath().normalize();
        if (!Files.exists(candidateWorkspacePath)) {
            // Lazy auto-initialization if not yet started
            Path originalRepoPath = Paths.get(STORAGE_BASE_DIR, assessmentId.toString(), "original").toAbsolutePath().normalize();
            try {
                if (!Files.exists(originalRepoPath) && assessment.getRepositoryUrl() != null && !assessment.getRepositoryUrl().trim().isEmpty()) {
                    log.info("Cloning repository on demand for candidate workspace resolution: {}", assessment.getRepositoryUrl());
                    gitCloningService.cloneRepository(assessmentId, assessment.getRepositoryUrl(), assessment.getBranchName());
                }

                Files.createDirectories(candidateWorkspacePath);
                if (Files.exists(originalRepoPath)) {
                    copyDirectoryTree(originalRepoPath, candidateWorkspacePath);
                    createStarterFilesIfMissing(candidateWorkspacePath);
                } else {
                    createStarterFilesIfMissing(candidateWorkspacePath);
                }
            } catch (Exception e) {
                log.error("Could not initialize candidate workspace directory", e);
                try {
                    createStarterFilesIfMissing(candidateWorkspacePath);
                } catch (IOException ignored) {}
            }
        }
        return candidateWorkspacePath;
    }

    private Path sanitizeAndResolvePath(Path rootDir, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new BadRequestException("Path must not be empty");
        }
        Path resolved = rootDir.resolve(relativePath.trim()).normalize();
        if (!resolved.startsWith(rootDir)) {
            throw new ForbiddenException("Access outside candidate workspace directory is forbidden");
        }
        return resolved;
    }

    private FileNodeDto buildFileNode(File file, Path rootDir) {
        String relativePath = rootDir.relativize(file.toPath()).toString().replace("\\", "/");
        String name = file.getName();
        if (relativePath.isEmpty()) {
            name = "root";
        }

        if (file.isDirectory()) {
            FileNodeDto node = FileNodeDto.builder()
                    .name(name)
                    .type("DIRECTORY")
                    .path(relativePath)
                    .children(new ArrayList<>())
                    .build();

            File[] children = file.listFiles();
            if (children != null) {
                // Sort directories first, then alphabetical
                Arrays.sort(children, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });

                for (File child : children) {
                    if (shouldIgnoreForUI(child.getName())) continue;
                    node.getChildren().add(buildFileNode(child, rootDir));
                }
            }
            return node;
        } else {
            String ext = "";
            int dotIdx = name.lastIndexOf('.');
            if (dotIdx > 0) {
                ext = name.substring(dotIdx + 1).toLowerCase();
            }
            return FileNodeDto.builder()
                    .name(name)
                    .type("FILE")
                    .path(relativePath)
                    .extension(ext)
                    .sizeBytes(file.length())
                    .build();
        }
    }

    private boolean shouldIgnoreForUI(String name) {
        if (IGNORED_UI_NAMES.contains(name)) return true;
        for (String ext : IGNORED_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private boolean shouldIgnoreForCopy(String name) {
        if (IGNORED_COPY_NAMES.contains(name)) return true;
        for (String ext : IGNORED_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private String detectLanguage(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".xml")) return "xml";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) return "yaml";
        if (lower.endsWith(".properties")) return "properties";
        if (lower.endsWith(".md")) return "markdown";
        if (lower.endsWith(".sql")) return "sql";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".css")) return "css";
        if (lower.endsWith(".js") || lower.endsWith(".jsx")) return "javascript";
        if (lower.endsWith(".ts") || lower.endsWith(".tsx")) return "typescript";
        return "plaintext";
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

    private void deleteRecursively(Path root) throws IOException {
        if (Files.exists(root)) {
            try (Stream<Path> walk = Files.walk(root)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
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
