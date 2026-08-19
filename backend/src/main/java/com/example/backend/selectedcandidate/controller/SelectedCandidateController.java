package com.example.backend.selectedcandidate.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.selectedcandidate.dto.SelectCandidateRequest;
import com.example.backend.selectedcandidate.dto.SelectedCandidateItemDto;
import com.example.backend.selectedcandidate.service.SelectedCandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/selected-candidates")
public class SelectedCandidateController {

    private final SelectedCandidateService selectedCandidateService;

    public SelectedCandidateController(SelectedCandidateService selectedCandidateService) {
        this.selectedCandidateService = selectedCandidateService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SelectedCandidateItemDto>> selectCandidate(
            @Valid @RequestBody SelectCandidateRequest request) {
        SelectedCandidateItemDto response = selectedCandidateService.selectCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Candidate marked as selected successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SelectedCandidateItemDto>>> getSelectedCandidates(
            @RequestParam(required = false) UUID workspaceId) {
        List<SelectedCandidateItemDto> response = selectedCandidateService.getSelectedCandidates(workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Selected candidates retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSelectedCandidate(@PathVariable UUID id) {
        selectedCandidateService.removeSelectedCandidate(id);
        return ResponseEntity.ok(ApiResponse.success("Selected candidate removed successfully", null));
    }
}
