package com.example.backend.assessment.controller;

import com.example.backend.assessment.dto.workspace.*;
import com.example.backend.assessment.service.CandidateWorkspaceService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller providing candidate IDE workspace operations: workspace provisioning,
 * file system exploration, Monaco Editor file loading, autosaving, and file management.
 */
@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateWorkspaceController {

    private final CandidateWorkspaceService candidateWorkspaceService;

    /**
     * Phase A.1: Initializes assessment sandbox and creates candidate workspace.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing StartAssessmentResponse.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartAssessmentResponse>> startAssessment(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Start assessment {} by candidate {}", aId, candidateId);
        StartAssessmentResponse response = candidateWorkspaceService.startAssessment(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Assessment workspace initialized successfully", response));
    }

    /**
     * Phase A.2: Fetches hierarchical file explorer directory tree for Monaco IDE.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing root FileNodeDto tree.
     */
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<FileNodeDto>> getFileTree(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Fetch file tree for assessment {}", aId);
        FileNodeDto tree = candidateWorkspaceService.getFileTree(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * Phase A.3: Reads source code file content for Monaco Editor.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param path              Relative path of the file to load.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing FileContentResponse with text and detected language.
     */
    @GetMapping("/files/content")
    public ResponseEntity<ApiResponse<FileContentResponse>> getFileContent(
            @PathVariable String assessmentId,
            @RequestParam("path") String path,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Read file content at '{}' for assessment {}", path, aId);
        FileContentResponse response = candidateWorkspaceService.getFileContent(candidateId, aId, path);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase A.4: Saves modified file content from Monaco Editor.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param request           DTO with relative path and updated file content.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse with confirmation.
     */
    @PutMapping("/files/content")
    public ResponseEntity<ApiResponse<Void>> saveFileContent(
            @PathVariable String assessmentId,
            @Valid @RequestBody SaveFileRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Save file content at '{}' for assessment {}", request.getPath(), aId);
        candidateWorkspaceService.saveFileContent(candidateId, aId, request);
        return ResponseEntity.ok(ApiResponse.success("File saved successfully", null));
    }

    /**
     * Phase A.5: Creates a new file or directory in candidate workspace.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param request           DTO specifying path and type (FILE / DIRECTORY).
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse with HTTP 201 Created.
     */
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<Void>> createFileOrDirectory(
            @PathVariable String assessmentId,
            @Valid @RequestBody CreateFileRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Create {} at '{}' for assessment {}", request.getType(), request.getPath(), aId);
        candidateWorkspaceService.createFileOrDirectory(candidateId, aId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created successfully", null));
    }

    /**
     * Phase A.5: Deletes a file or directory from candidate workspace.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param path              Relative path to delete.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse with confirmation.
     */
    @DeleteMapping("/files")
    public ResponseEntity<ApiResponse<Void>> deleteFileOrDirectory(
            @PathVariable String assessmentId,
            @RequestParam("path") String path,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Delete '{}' for assessment {}", path, aId);
        candidateWorkspaceService.deleteFileOrDirectory(candidateId, aId, path);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }

    /**
     * Phase A.5: Renames or moves a file / directory in candidate workspace.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param request           DTO specifying oldPath and newPath.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse with confirmation.
     */
    @PostMapping("/files/rename")
    public ResponseEntity<ApiResponse<Void>> renameFileOrDirectory(
            @PathVariable String assessmentId,
            @Valid @RequestBody RenameFileRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Rename '{}' to '{}' for assessment {}", request.getOldPath(), request.getNewPath(), aId);
        candidateWorkspaceService.renameFileOrDirectory(candidateId, aId, request);
        return ResponseEntity.ok(ApiResponse.success("Renamed successfully", null));
    }
}
