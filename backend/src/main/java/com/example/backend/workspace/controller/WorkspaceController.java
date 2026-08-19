package com.example.backend.workspace.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.workspace.dto.*;
import com.example.backend.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId) {
        List<WorkspaceResponse> response = workspaceService.getWorkspaces(recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved successfully", response));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceById(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId) {
        WorkspaceResponse response = workspaceService.getWorkspaceById(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved successfully", response));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(recruiterId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully", Map.of("message", "Workspace deleted successfully")));
    }

    @GetMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<List<WorkspaceCandidateResponse>>> getCandidatesInWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId) {
        List<WorkspaceCandidateResponse> response = workspaceService.getCandidatesInWorkspace(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace candidates retrieved successfully", response));
    }

    @PostMapping("/{workspaceId}/candidates")
    public ResponseEntity<ApiResponse<AddCandidateResponse>> addCandidateToWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddCandidateToWorkspaceRequest request) {
        AddCandidateResponse response = workspaceService.addCandidateToWorkspace(recruiterId, workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate added to workspace successfully", response));
    }

    @DeleteMapping("/{workspaceId}/candidates/{candidateId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> removeCandidateFromWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID candidateId) {
        workspaceService.removeCandidateFromWorkspace(recruiterId, workspaceId, candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate removed from workspace successfully", Map.of("message", "Candidate removed from workspace successfully")));
    }
}
