package com.example.backend.workspace.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import com.example.backend.workspace.dto.*;
import com.example.backend.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        log.info("Creating workspace with name: {}", request.getName());
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        WorkspaceResponse response = workspaceService.createWorkspace(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader) {
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        List<WorkspaceResponse> response = workspaceService.getWorkspaces(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceDetailResponse>> getWorkspaceById(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        WorkspaceDetailResponse response = workspaceService.getWorkspaceById(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        WorkspaceResponse response = workspaceService.updateWorkspace(recruiterId, wsId, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        workspaceService.deleteWorkspace(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted", Map.of("id", wsId)));
    }

    @GetMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<List<WorkspaceCandidateItemResponse>>> getCandidatesInWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        List<WorkspaceCandidateItemResponse> response = workspaceService.getCandidatesInWorkspace(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<WorkspaceCandidateItemResponse>> addCandidateToWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId,
            @Valid @RequestBody AddCandidateRequest request) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        WorkspaceCandidateItemResponse response = workspaceService.addCandidateToWorkspace(recruiterId, wsId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate added to workspace", response));
    }

    @DeleteMapping("/{workspaceId}/candidates/{candidateId}")
    public ResponseEntity<ApiResponse<Map<String, UUID>>> removeCandidateFromWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId,
            @PathVariable String candidateId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        UUID candId = UUIDUtils.parseUuidOrNull(candidateId);
        if (wsId == null || candId == null) {
            throw new BadRequestException("Invalid ID format: must be valid UUIDs");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        workspaceService.removeCandidateFromWorkspace(recruiterId, wsId, candId);
        return ResponseEntity.ok(ApiResponse.success("Candidate removed from workspace", Map.of(
                "workspaceId", wsId,
                "candidateId", candId
        )));
    }
}
