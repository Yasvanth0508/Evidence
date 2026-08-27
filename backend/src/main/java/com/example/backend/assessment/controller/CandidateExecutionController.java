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

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateExecutionController {

    private final CandidateExecutionService executionService;

    /**
     * Phase B.1: Trigger application compilation and execution.
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ExecutionRunResponse>> runCandidateApplication(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Run application for assessment {} by candidate {}", aId, candidateId);
        ExecutionRunResponse response = executionService.runCandidateApplication(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Application compilation and execution started", response));
    }

    /**
     * Phase B.2: Check current application execution status.
     */
    @GetMapping("/execution/status")
    public ResponseEntity<ApiResponse<ExecutionStatusResponse>> getExecutionStatus(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Get execution status for assessment {}", aId);
        ExecutionStatusResponse response = executionService.getExecutionStatus(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase B.3: Fetch live execution logs and terminal output.
     */
    @GetMapping("/execution/logs")
    public ResponseEntity<ApiResponse<ExecutionLogsResponse>> getExecutionLogs(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Fetch execution logs for assessment {}", aId);
        ExecutionLogsResponse response = executionService.getExecutionLogs(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase B.4: Stop running application container or process.
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<StopExecutionResponse>> stopCandidateApplication(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Stop application for assessment {}", aId);
        StopExecutionResponse response = executionService.stopCandidateApplication(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Container stopped successfully", response));
    }
}
