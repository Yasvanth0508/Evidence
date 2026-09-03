package com.example.backend.assessment.controller;

import com.example.backend.assessment.dto.evaluation.CandidateResultResponse;
import com.example.backend.assessment.dto.evaluation.RecruiterReportResponse;
import com.example.backend.assessment.dto.evaluation.RecruiterTestResultItemDto;
import com.example.backend.assessment.dto.evaluation.SubmissionResponse;
import com.example.backend.assessment.service.CandidateEvaluationService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller handling assessment submissions, scoring calculations, and recruiter evaluation reports.
 */
@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateEvaluationController {

    private final CandidateEvaluationService evaluationService;

    /**
     * Phase C.1: Triggers final assessment submission and automated black-box evaluation.
     *
     * @param assessmentId      UUID string of the assessment being submitted.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing the SubmissionResponse with evaluation outcome.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssessment(
            @PathVariable String assessmentId,
            @RequestBody(required = false) com.example.backend.assessment.dto.evaluation.SubmitAssessmentRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Submit assessment {} by candidate {} with request={}", aId, candidateId, request);
        SubmissionResponse response = evaluationService.submitAssessment(candidateId, aId, request);
        return ResponseEntity.ok(ApiResponse.success("Assessment submitted successfully. Evaluation completed.", response));
    }

    /**
     * Phase C.5: Retrieves candidate-safe summary evaluation result.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param candidateIdHeader Optional or injected X-Candidate-Id header.
     * @return ApiResponse containing CandidateResultResponse.
     */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<CandidateResultResponse>> getCandidateResult(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID candidateId = principal.getId();
        log.info("REST: Fetch candidate result for assessment {}", aId);
        CandidateResultResponse response = evaluationService.getCandidateResult(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase D.2: Retrieves full recruiter evaluation report.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param recruiterIdHeader Optional or injected X-Recruiter-Id header.
     * @return ApiResponse containing RecruiterReportResponse.
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<RecruiterReportResponse>> getRecruiterReport(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        log.info("REST: Fetch recruiter report for assessment {}", aId);
        RecruiterReportResponse response = evaluationService.getRecruiterReport(recruiterId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase D.3: Retrieves granular per-test-case results breakdown for recruiters.
     *
     * @param assessmentId      UUID string of the assessment.
     * @param recruiterIdHeader Optional or injected X-Recruiter-Id header.
     * @return ApiResponse containing list of RecruiterTestResultItemDto.
     */
    @GetMapping("/test-results")
    public ResponseEntity<ApiResponse<List<RecruiterTestResultItemDto>>> getRecruiterTestResults(
            @PathVariable String assessmentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        log.info("REST: Fetch recruiter test results for assessment {}", aId);
        List<RecruiterTestResultItemDto> results = evaluationService.getRecruiterTestResults(recruiterId, aId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
