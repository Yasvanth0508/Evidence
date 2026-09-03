package com.example.backend.workspace.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import com.example.backend.workspace.dto.*;
import com.example.backend.workspace.service.WorkspaceService;
import com.example.backend.workspace.service.WorkspaceCandidateService;
import com.example.backend.workspace.service.WorkspaceDeletionService;
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
    private final WorkspaceCandidateService workspaceCandidateService;
    private final WorkspaceDeletionService workspaceDeletionService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        log.info("Creating workspace with name: {}", request.getName());
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        WorkspaceResponse response = workspaceService.createWorkspace(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        List<WorkspaceResponse> response = workspaceService.getWorkspaces(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceDetailResponse>> getWorkspaceById(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        WorkspaceDetailResponse response = workspaceService.getWorkspaceById(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        WorkspaceResponse response = workspaceService.updateWorkspace(recruiterId, wsId, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        workspaceDeletionService.deleteWorkspace(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted", Map.of("id", wsId)));
    }

    @GetMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<List<WorkspaceCandidateItemResponse>>> getCandidatesInWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        List<WorkspaceCandidateItemResponse> response = workspaceCandidateService.getCandidatesInWorkspace(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<WorkspaceCandidateItemResponse>> addCandidateToWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId,
            @Valid @RequestBody AddCandidateRequest request) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        WorkspaceCandidateItemResponse response = workspaceCandidateService.addCandidateToWorkspace(recruiterId, wsId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate added to workspace", response));
    }

    @DeleteMapping("/{workspaceId}/candidates/{candidateId}")
    public ResponseEntity<ApiResponse<Map<String, UUID>>> removeCandidateFromWorkspace(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String workspaceId,
            @PathVariable String candidateId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        UUID candId = UUIDUtils.parseUuidOrNull(candidateId);
        if (wsId == null || candId == null) {
            throw new BadRequestException("Invalid ID format: must be valid UUIDs");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        workspaceCandidateService.removeCandidateFromWorkspace(recruiterId, wsId, candId);
        return ResponseEntity.ok(ApiResponse.success("Candidate removed from workspace", Map.of(
                "workspaceId", wsId,
                "candidateId", candId
        )));
    }
}
