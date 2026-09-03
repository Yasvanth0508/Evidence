package com.example.backend.dashboard.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.dashboard.service.CandidateDashboardService;
import com.example.backend.dashboard.service.RecruiterDashboardService;
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

    private final RecruiterDashboardService recruiterDashboardService;
    private final CandidateDashboardService candidateDashboardService;

    @GetMapping("/recruiter/dashboard")
    public ResponseEntity<ApiResponse<RecruiterDashboardResponse>> getRecruiterDashboard(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UUID recruiterId = principal.getId();
        log.info("Fetching recruiter dashboard for ID: {}", recruiterId);
        RecruiterDashboardResponse response = recruiterDashboardService.getRecruiterDashboard(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/candidate/dashboard")
    public ResponseEntity<ApiResponse<CandidateDashboardResponse>> getCandidateDashboard(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UUID candidateId = principal.getId();
        log.info("Fetching candidate dashboard for ID: {}", candidateId);
        CandidateDashboardResponse response = candidateDashboardService.getCandidateDashboard(candidateId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
