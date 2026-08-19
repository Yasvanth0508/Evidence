package com.example.backend.assessment.controller;

import com.example.backend.assessment.dto.*;
import com.example.backend.assessment.service.AssessmentService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.Role;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/workspaces/{workspaceId}/assessments")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateAssessmentRequest request) {
        AssessmentResponse response = assessmentService.createAssessment(recruiterId, workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assessment creation initiated", response));
    }

    @GetMapping("/workspaces/{workspaceId}/assessments")
    public ResponseEntity<ApiResponse<List<AssessmentListItemResponse>>> getAssessmentsByWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID workspaceId) {
        List<AssessmentListItemResponse> response = assessmentService.getAssessmentsByWorkspace(recruiterId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Assessments retrieved successfully", response));
    }

    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessmentById(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) Role role,
            @PathVariable UUID assessmentId) {
        AssessmentResponse response = assessmentService.getAssessmentById(userId, role, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment retrieved successfully", response));
    }

    @PutMapping("/assessments/{assessmentId}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> updateAssessment(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID assessmentId,
            @RequestBody UpdateAssessmentRequest request) {
        AssessmentResponse response = assessmentService.updateAssessment(recruiterId, assessmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Assessment updated successfully", response));
    }

    @PostMapping("/assessments/{assessmentId}/cancel")
    public ResponseEntity<ApiResponse<AssessmentResponse>> cancelAssessment(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID assessmentId) {
        AssessmentResponse response = assessmentService.cancelAssessment(recruiterId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment cancelled successfully", response));
    }

    @GetMapping("/assessments/{assessmentId}/processing-status")
    public ResponseEntity<ApiResponse<ProcessingStatusResponse>> getProcessingStatus(
            @RequestHeader(value = "X-Recruiter-Id", required = false) UUID recruiterId,
            @PathVariable UUID assessmentId) {
        ProcessingStatusResponse response = assessmentService.getProcessingStatus(recruiterId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Processing status retrieved successfully", response));
    }

    @PostMapping("/assessments/{assessmentId}/start")
    public ResponseEntity<ApiResponse<StartAssessmentResponse>> startAssessment(
            @RequestHeader(value = "X-Candidate-Id", required = false) UUID candidateId,
            @PathVariable UUID assessmentId) {
        StartAssessmentResponse response = assessmentService.startAssessment(candidateId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment started successfully", response));
    }

    @PostMapping("/assessments/{assessmentId}/submit")
    public ResponseEntity<ApiResponse<SubmitAssessmentResponse>> submitAssessment(
            @RequestHeader(value = "X-Candidate-Id", required = false) UUID candidateId,
            @PathVariable UUID assessmentId) {
        SubmitAssessmentResponse response = assessmentService.submitAssessment(candidateId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment submitted successfully", response));
    }
}
