package com.example.backend.candidate.controller;

import com.example.backend.candidate.dto.CandidateAssessmentDto;
import com.example.backend.candidate.dto.CandidateResponse;
import com.example.backend.candidate.dto.CandidateWorkspaceDto;
import com.example.backend.candidate.service.CandidateService;
import com.example.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CandidateResponse>> searchCandidateByEmail(@RequestParam String email) {
        CandidateResponse response = candidateService.searchByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Candidate found", response));
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(@PathVariable UUID candidateId) {
        CandidateResponse response = candidateService.getCandidateById(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate retrieved successfully", response));
    }

    @GetMapping("/{candidateId}/workspaces")
    public ResponseEntity<ApiResponse<List<CandidateWorkspaceDto>>> getCandidateWorkspaces(@PathVariable UUID candidateId) {
        List<CandidateWorkspaceDto> response = candidateService.getCandidateWorkspaces(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate workspaces retrieved successfully", response));
    }

    @GetMapping("/{candidateId}/assessments")
    public ResponseEntity<ApiResponse<List<CandidateAssessmentDto>>> getCandidateAssessments(@PathVariable UUID candidateId) {
        List<CandidateAssessmentDto> response = candidateService.getCandidateAssessments(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate assessments retrieved successfully", response));
    }
}
