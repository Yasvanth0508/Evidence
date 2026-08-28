package com.example.backend.assessment.controller;

import com.example.backend.assessment.dto.execution.ExecutionLogsResponse;
import com.example.backend.assessment.dto.execution.ExecutionRunResponse;
import com.example.backend.assessment.dto.execution.ExecutionStatusResponse;
import com.example.backend.assessment.dto.execution.StopExecutionResponse;
import com.example.backend.assessment.service.CandidateExecutionService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller managing application compilation, container execution, live terminal logs,
 * and application lifecycle controls.
 */
@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateExecutionController {

    private final CandidateExecutionService executionService;

    /**
     * Phase B.1: Compiles candidate application and launches ephemeral container/sandbox.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing ExecutionRunResponse.
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ExecutionRunResponse>> runCandidateApplication(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Run application for assessment {} by candidate {}", aId, candidateId);
        ExecutionRunResponse response = executionService.runCandidateApplication(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Application compilation and execution started", response));
    }

    /**
     * Phase B.2: Checks current application execution status and container uptime.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing ExecutionStatusResponse.
     */
    @GetMapping("/execution/status")
    public ResponseEntity<ApiResponse<ExecutionStatusResponse>> getExecutionStatus(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Get execution status for assessment {}", aId);
        ExecutionStatusResponse response = executionService.getExecutionStatus(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase B.3: Fetches live streaming execution logs and terminal output.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing ExecutionLogsResponse.
     */
    @GetMapping("/execution/logs")
    public ResponseEntity<ApiResponse<ExecutionLogsResponse>> getExecutionLogs(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Fetch execution logs for assessment {}", aId);
        ExecutionLogsResponse response = executionService.getExecutionLogs(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase B.4: Terminates running candidate container or native process.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing StopExecutionResponse.
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<StopExecutionResponse>> stopCandidateApplication(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Stop application for assessment {}", aId);
        StopExecutionResponse response = executionService.stopCandidateApplication(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Container stopped successfully", response));
    }
}
