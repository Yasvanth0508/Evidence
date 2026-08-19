package com.example.backend.analysis.controller;

import com.example.backend.analysis.dto.AnalysisStatusResponse;
import com.example.backend.analysis.dto.RepositoryAnalysisResponse;
import com.example.backend.analysis.service.RepositoryAnalysisService;
import com.example.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/repository-analysis")
public class RepositoryAnalysisController {

    private final RepositoryAnalysisService repositoryAnalysisService;

    public RepositoryAnalysisController(RepositoryAnalysisService repositoryAnalysisService) {
        this.repositoryAnalysisService = repositoryAnalysisService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RepositoryAnalysisResponse>> getRepositoryAnalysis(
            @PathVariable UUID assessmentId) {
        RepositoryAnalysisResponse response = repositoryAnalysisService.getAnalysis(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Repository analysis retrieved successfully", response));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AnalysisStatusResponse>> getRepositoryAnalysisStatus(
            @PathVariable UUID assessmentId) {
        AnalysisStatusResponse response = repositoryAnalysisService.getAnalysisStatus(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Repository analysis status retrieved successfully", response));
    }
}
