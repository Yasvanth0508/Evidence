package com.example.backend.report.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.util.UUIDUtils;
import com.example.backend.report.dto.ReportPageResponse;
import com.example.backend.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Phase D.1: List Workspace Assessment Reports with Pagination.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ReportPageResponse>> getWorkspaceReports(
            @RequestParam("workspaceId") UUID workspaceId,
            @RequestParam(value = "status", required = false) AssessmentStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader) {

        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        log.info("REST: Fetch workspace reports for workspace {}, status={}, page={}, size={}", workspaceId, status, page, size);
        ReportPageResponse response = reportService.getWorkspaceReports(recruiterId, workspaceId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
