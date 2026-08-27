package com.example.backend.assessment.controller;

import com.example.backend.assessment.dto.AssessmentResponse;
import com.example.backend.assessment.dto.AssessmentStatusResponse;
import com.example.backend.assessment.dto.CreateAssessmentRequest;
import com.example.backend.assessment.dto.FeatureSpecificationResponse;
import com.example.backend.assessment.dto.ProcessingStatusResponse;
import com.example.backend.assessment.service.AssessmentService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping("/workspaces/{workspaceId}/assessments")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId,
            @Valid @RequestBody CreateAssessmentRequest request) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        log.info("Creating assessment in workspace ID: {} for candidate ID: {}", wsId, request.getCandidateId());
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        AssessmentResponse response = assessmentService.createAssessment(recruiterId, wsId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assessment creation started", response));
    }

    @GetMapping("/workspaces/{workspaceId}/assessments")
    public ResponseEntity<ApiResponse<java.util.List<AssessmentResponse>>> getAssessmentsByWorkspace(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String workspaceId) {
        UUID wsId = UUIDUtils.parseUuidOrNull(workspaceId);
        if (wsId == null) {
            throw new BadRequestException("Invalid workspace ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        java.util.List<AssessmentResponse> response = assessmentService.getAssessmentsByWorkspace(recruiterId, wsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<ApiResponse<Object>> getAssessmentDetails(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader,
            @PathVariable String assessmentId) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        Object response = assessmentService.getAssessmentDetails(recruiterId, candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assessments/{assessmentId}/processing-status")
    public ResponseEntity<ApiResponse<ProcessingStatusResponse>> getProcessingStatus(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @PathVariable String assessmentId) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        ProcessingStatusResponse response = assessmentService.getProcessingStatus(recruiterId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assessments/{assessmentId}/feature")
    public ResponseEntity<ApiResponse<FeatureSpecificationResponse>> getFeatureSpecification(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader,
            @PathVariable String assessmentId) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        FeatureSpecificationResponse response = assessmentService.getFeatureSpecification(recruiterId, candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assessments/{assessmentId}/repository-analysis")
    public ResponseEntity<ApiResponse<Object>> getRepositoryAnalysis(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader,
            @PathVariable String assessmentId) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        Object response = assessmentService.getRepositoryAnalysis(recruiterId, candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assessments/{assessmentId}/status")
    public ResponseEntity<ApiResponse<AssessmentStatusResponse>> getAssessmentStatus(
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader,
            @PathVariable String assessmentId) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        AssessmentStatusResponse response = assessmentService.getAssessmentStatus(recruiterId, candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
