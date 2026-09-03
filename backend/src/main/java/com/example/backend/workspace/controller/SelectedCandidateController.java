package com.example.backend.workspace.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.UnauthorizedException;
import com.example.backend.workspace.dto.SelectedCandidateRequest;
import com.example.backend.workspace.dto.SelectedCandidateResponse;
import com.example.backend.workspace.service.SelectedCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/selected-candidates")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class SelectedCandidateController {

    private final SelectedCandidateService selectedCandidateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SelectedCandidateResponse>>> getSelectedCandidates(
            @RequestParam(value = "workspaceId", required = false) UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Fetch selected candidates for recruiter={}, workspace={}", recruiterId, workspaceId);
        List<SelectedCandidateResponse> response = selectedCandidateService.getSelectedCandidates(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SelectedCandidateResponse>> selectCandidate(
            @Valid @RequestBody SelectedCandidateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Select candidate {} by recruiter {}", request.getCandidateId(), recruiterId);
        SelectedCandidateResponse response = selectedCandidateService.selectCandidate(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate marked as selected", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSelectedCandidate(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Remove selected candidate {} by recruiter {}", id, recruiterId);
        selectedCandidateService.removeSelectedCandidate(recruiterId, id);
        return ResponseEntity.ok(ApiResponse.success("Selected candidate removed", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeSelectedCandidateByWorkspaceAndCandidate(
            @RequestParam("workspaceId") UUID workspaceId,
            @RequestParam("candidateId") UUID candidateId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Remove selected candidate {} in workspace {} by recruiter {}", candidateId, workspaceId, recruiterId);
        selectedCandidateService.removeSelectedCandidateByWorkspaceAndCandidate(recruiterId, workspaceId, candidateId);
        return ResponseEntity.ok(ApiResponse.success("Selected candidate removed", null));
    }
}
