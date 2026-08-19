package com.example.backend.dashboard.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.service.CandidateDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/candidate/dashboard")
public class CandidateDashboardController {

    private final CandidateDashboardService candidateDashboardService;

    public CandidateDashboardController(CandidateDashboardService candidateDashboardService) {
        this.candidateDashboardService = candidateDashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CandidateDashboardResponse>> getCandidateDashboard() {
        CandidateDashboardResponse response = candidateDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Candidate dashboard retrieved successfully", response));
    }
}
