package com.example.backend.report.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.report.dto.ReportPageResponse;
import com.example.backend.report.dto.ReportSummaryResponse;
import com.example.backend.report.service.ReportQueryService;
import com.example.backend.report.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller serving assessment evaluation reports and high-level analytical summaries for recruiters.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final ReportSummaryService reportSummaryService;

    /**
     * Lists assessment reports with optional workspace and status filters, supporting pagination.
     *
     * @param workspaceId Optional workspace UUID filter.
     * @param status      Optional assessment status filter.
     * @param page        Page number (default 0).
     * @param size        Page size (default 20).
     * @return ApiResponse containing paginated ReportPageResponse.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ReportPageResponse>> getWorkspaceReports(
            @RequestParam(value = "workspaceId", required = false) UUID workspaceId,
            @RequestParam(value = "status", required = false) AssessmentStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Fetch reports for workspace={}, status={}, page={}, size={}", workspaceId, status, page, size);
        ReportPageResponse response = reportQueryService.getWorkspaceReports(recruiterId, workspaceId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves aggregated KPI analytics (pass rates, average score, candidate participation).
     *
     * @param workspaceId Optional workspace UUID filter.
     * @param principal   Authenticated user principal.
     * @return ApiResponse containing ReportSummaryResponse.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getReportSummary(
            @RequestParam(value = "workspaceId", required = false) UUID workspaceId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        UUID recruiterId = principal.getId();
        log.info("REST: Fetch report summary for workspace={}, recruiter={}", workspaceId, recruiterId);
        ReportSummaryResponse summary = reportSummaryService.getReportSummary(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
