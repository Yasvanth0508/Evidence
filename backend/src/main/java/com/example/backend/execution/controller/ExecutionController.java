package com.example.backend.execution.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.execution.dto.ExecutionLogsResponse;
import com.example.backend.execution.dto.ExecutionResponse;
import com.example.backend.execution.dto.ExecutionStatusResponse;
import com.example.backend.execution.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<ExecutionResponse>> runApplication(@PathVariable UUID assessmentId) {
        ExecutionResponse response = executionService.runApplication(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Application started successfully", response));
    }

    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<ExecutionResponse>> stopApplication(@PathVariable UUID assessmentId) {
        ExecutionResponse response = executionService.stopApplication(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Application stopped successfully", response));
    }

    @GetMapping("/execution/status")
    public ResponseEntity<ApiResponse<ExecutionStatusResponse>> getExecutionStatus(@PathVariable UUID assessmentId) {
        ExecutionStatusResponse response = executionService.getExecutionStatus(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Execution status retrieved successfully", response));
    }

    @GetMapping("/execution/logs")
    public ResponseEntity<ApiResponse<ExecutionLogsResponse>> getExecutionLogs(@PathVariable UUID assessmentId) {
        ExecutionLogsResponse response = executionService.getExecutionLogs(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Execution logs retrieved successfully", response));
    }
}
