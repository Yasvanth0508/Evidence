package com.example.backend.dashboard.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.dashboard.dto.RecruiterDashboardResponse;
import com.example.backend.dashboard.service.RecruiterDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruiter/dashboard")
public class RecruiterDashboardController {

    private final RecruiterDashboardService recruiterDashboardService;

    public RecruiterDashboardController(RecruiterDashboardService recruiterDashboardService) {
        this.recruiterDashboardService = recruiterDashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RecruiterDashboardResponse>> getRecruiterDashboard() {
        RecruiterDashboardResponse response = recruiterDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", response));
    }
}
