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

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateWorkspaceController {

    private final CandidateWorkspaceService candidateWorkspaceService;

    /**
     * Phase A.1: Start assessment & initialize candidate workspace.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartAssessmentResponse>> startAssessment(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Start assessment {} by candidate {}", aId, candidateId);
        StartAssessmentResponse response = candidateWorkspaceService.startAssessment(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Assessment workspace initialized successfully", response));
    }

    /**
     * Phase A.2: Fetch hierarchical file explorer directory tree.
     */
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<FileNodeDto>> getFileTree(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Fetch file tree for assessment {}", aId);
        FileNodeDto tree = candidateWorkspaceService.getFileTree(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * Phase A.3: Read file content for Monaco Editor.
     */
    @GetMapping("/files/content")
    public ResponseEntity<ApiResponse<FileContentResponse>> getFileContent(
            @PathVariable String assessmentId,
            @RequestParam("path") String path,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Read file content at '{}' for assessment {}", path, aId);
        FileContentResponse response = candidateWorkspaceService.getFileContent(candidateId, aId, path);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase A.4: Save / debounced autosave file content from Monaco Editor.
     */
    @PutMapping("/files/content")
    public ResponseEntity<ApiResponse<Void>> saveFileContent(
            @PathVariable String assessmentId,
            @Valid @RequestBody SaveFileRequest request,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Save file content at '{}' for assessment {}", request.getPath(), aId);
        candidateWorkspaceService.saveFileContent(candidateId, aId, request);
        return ResponseEntity.ok(ApiResponse.success("File saved successfully", null));
    }

    /**
     * Phase A.5: Create new file or directory.
     */
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<Void>> createFileOrDirectory(
            @PathVariable String assessmentId,
            @Valid @RequestBody CreateFileRequest request,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Create {} at '{}' for assessment {}", request.getType(), request.getPath(), aId);
        candidateWorkspaceService.createFileOrDirectory(candidateId, aId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created successfully", null));
    }

    /**
     * Phase A.5: Delete file or directory.
     */
    @DeleteMapping("/files")
    public ResponseEntity<ApiResponse<Void>> deleteFileOrDirectory(
            @PathVariable String assessmentId,
            @RequestParam("path") String path,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Delete '{}' for assessment {}", path, aId);
        candidateWorkspaceService.deleteFileOrDirectory(candidateId, aId, path);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }

    /**
     * Phase A.5: Rename or move file / directory.
     */
    @PostMapping("/files/rename")
    public ResponseEntity<ApiResponse<Void>> renameFileOrDirectory(
            @PathVariable String assessmentId,
            @Valid @RequestBody RenameFileRequest request,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {

        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Rename '{}' to '{}' for assessment {}", request.getOldPath(), request.getNewPath(), aId);
        candidateWorkspaceService.renameFileOrDirectory(candidateId, aId, request);
        return ResponseEntity.ok(ApiResponse.success("Renamed successfully", null));
    }
}
