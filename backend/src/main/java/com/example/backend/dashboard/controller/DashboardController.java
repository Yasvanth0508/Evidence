package com.example.backend.dashboard.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.util.UUIDUtils;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/recruiter/dashboard")
    public ResponseEntity<ApiResponse<RecruiterDashboardResponse>> getRecruiterDashboard(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader) {
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        log.info("Fetching recruiter dashboard for ID: {}", recruiterId);
        RecruiterDashboardResponse response = dashboardService.getRecruiterDashboard(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/candidate/dashboard")
    public ResponseEntity<ApiResponse<CandidateDashboardResponse>> getCandidateDashboard(
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("Fetching candidate dashboard for ID: {}", candidateId);
        CandidateDashboardResponse response = dashboardService.getCandidateDashboard(candidateId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
