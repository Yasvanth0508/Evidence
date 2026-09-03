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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateWorkspaceService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentWorkspaceRepository assessmentWorkspaceRepository;
    private final com.example.backend.pipeline.workspace.WorkspacePathService pathService;
    private final com.example.backend.pipeline.workspace.FileService fileService;
    private final com.example.backend.pipeline.workspace.RepositoryWorkspaceInitializer initializer;


    private static final Set<String> IGNORED_UI_NAMES = Set.of(
            ".git", "target", ".idea", "node_modules", ".mvn", ".settings", ".classpath", ".project", ".DS_Store"
    );
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".war", ".nar", ".zip", ".tar", ".gz"
    );

    @Transactional
    public StartAssessmentResponse startAssessment(UUID candidateId, UUID assessmentId) {
        log.info("Candidate {} is starting Assessment {}", candidateId, assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCandidate() == null || !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Instant now = Instant.now();
        if (assessment.getScheduledStartAt() != null && now.isBefore(assessment.getScheduledStartAt().minusSeconds(300))) {
            throw new AssessmentNotAvailableException("Assessment is not yet open. Scheduled start: " + assessment.getScheduledStartAt());
        }
        if (assessment.getScheduledEndAt() != null && now.isAfter(assessment.getScheduledEndAt())) {
            throw new AssessmentNotAvailableException("Assessment has already ended. Scheduled end: " + assessment.getScheduledEndAt());
        }

        Path originalRepoPath = pathService.resolveOriginalRepoPath(assessmentId);
        Path candidateWorkspacePath = pathService.resolveCandidateWorkspacePath(assessmentId);

        try {
            initializer.cloneRepositoryIfNeeded(assessmentId, assessment.getRepositoryUrl(), assessment.getBranchName(), originalRepoPath);
            initializer.initializeWorkspace(originalRepoPath, candidateWorkspacePath);
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

    @Transactional(readOnly = true)
    public FileNodeDto getFileTree(UUID candidateId, UUID assessmentId) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        return buildFileNode(workspaceDir.toFile(), workspaceDir);
    }

    @Transactional(readOnly = true)
    public FileContentResponse getFileContent(UUID candidateId, UUID assessmentId, String relativePath) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path targetFile = pathService.sanitizeAndResolvePath(workspaceDir, relativePath);

        if (!Files.exists(targetFile) || Files.isDirectory(targetFile)) {
            throw new ResourceNotFoundException("File not found: " + relativePath);
        }

        try {
            String content = fileService.readFile(targetFile);
            long size = fileService.getFileSize(targetFile);
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

    @Transactional(readOnly = true)
    public void saveFileContent(UUID candidateId, UUID assessmentId, SaveFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path targetFile = pathService.sanitizeAndResolvePath(workspaceDir, request.getPath());

        try {
            fileService.writeFile(targetFile, request.getContent());
            log.debug("Saved file content to {}", targetFile);
        } catch (IOException e) {
            log.error("Failed to save file content at {}", targetFile, e);
            throw new RuntimeException("Could not save file: " + request.getPath(), e);
        }
    }

    @Transactional(readOnly = true)
    public void createFileOrDirectory(UUID candidateId, UUID assessmentId, CreateFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path target = pathService.sanitizeAndResolvePath(workspaceDir, request.getPath());

        try {
            if ("DIRECTORY".equalsIgnoreCase(request.getType())) {
                fileService.createDirectory(target);
                log.info("Created directory in candidate workspace: {}", target);
            } else {
                fileService.writeFile(target, request.getInitialContent());
                log.info("Created file in candidate workspace: {}", target);
            }
        } catch (IOException e) {
            log.error("Failed to create file/directory at {}", target, e);
            throw new RuntimeException("Could not create " + request.getPath(), e);
        }
    }

    @Transactional(readOnly = true)
    public void deleteFileOrDirectory(UUID candidateId, UUID assessmentId, String relativePath) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path target = pathService.sanitizeAndResolvePath(workspaceDir, relativePath);

        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("File or directory not found: " + relativePath);
        }

        try {
            fileService.deleteRecursively(target);
            log.info("Deleted file/directory recursively in candidate workspace: {}", target);
        } catch (IOException e) {
            log.error("Failed to delete file/directory at {}", target, e);
            throw new RuntimeException("Could not delete " + relativePath, e);
        }
    }

    @Transactional(readOnly = true)
    public void renameFileOrDirectory(UUID candidateId, UUID assessmentId, RenameFileRequest request) {
        Path workspaceDir = resolveCandidateWorkspace(candidateId, assessmentId);
        Path oldTarget = pathService.sanitizeAndResolvePath(workspaceDir, request.getOldPath());
        Path newTarget = pathService.sanitizeAndResolvePath(workspaceDir, request.getNewPath());

        if (!Files.exists(oldTarget)) {
            throw new ResourceNotFoundException("Source file not found: " + request.getOldPath());
        }

        try {
            fileService.moveFile(oldTarget, newTarget);
            log.info("Renamed {} to {}", oldTarget, newTarget);
        } catch (IOException e) {
            log.error("Failed to rename {} to {}", oldTarget, newTarget, e);
            throw new RuntimeException("Could not rename file", e);
        }
    }

    public Path resolveCandidateWorkspace(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCandidate() == null || !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Path candidateWorkspacePath = pathService.resolveCandidateWorkspacePath(assessmentId);
        if (!Files.exists(candidateWorkspacePath)) {
            // Lazy auto-initialization if not yet started
            Path originalRepoPath = pathService.resolveOriginalRepoPath(assessmentId);
            try {
                initializer.cloneRepositoryIfNeeded(assessmentId, assessment.getRepositoryUrl(), assessment.getBranchName(), originalRepoPath);
                initializer.initializeWorkspace(originalRepoPath, candidateWorkspacePath);
            } catch (Exception e) {
                log.error("Could not initialize candidate workspace directory", e);
            }
        }
        return candidateWorkspacePath;
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
}
