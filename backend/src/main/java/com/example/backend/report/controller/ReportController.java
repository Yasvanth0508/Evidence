package com.example.backend.report.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.evaluation.dto.AssessmentReportResponse;
import com.example.backend.report.dto.ReportListResponse;
import com.example.backend.report.dto.ReportSummaryResponse;
import com.example.backend.report.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReportListResponse>> getReports(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) AssessmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ReportListResponse response = reportService.getReports(workspaceId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getReportSummary() {
        ReportSummaryResponse response = reportService.getReportSummary();
        return ResponseEntity.ok(ApiResponse.success("Report summary retrieved successfully", response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AssessmentReportResponse>> getReportById(@PathVariable UUID reportId) {
        AssessmentReportResponse response = reportService.getReportById(reportId);
        return ResponseEntity.ok(ApiResponse.success("Report details retrieved successfully", response));
    }
}
