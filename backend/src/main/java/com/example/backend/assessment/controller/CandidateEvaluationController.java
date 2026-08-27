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

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
public class CandidateEvaluationController {

    private final CandidateEvaluationService evaluationService;

    /**
     * Phase C.1: Final Assessment Submission & Evaluation Trigger.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssessment(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Submit assessment {} by candidate {}", aId, candidateId);
        SubmissionResponse response = evaluationService.submitAssessment(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success("Assessment submitted successfully. Evaluation completed.", response));
    }

    /**
     * Phase C.5: Candidate Safe Result View.
     */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<CandidateResultResponse>> getCandidateResult(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Candidate-Id", required = false) String candidateIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID candidateId = UUIDUtils.parseUuidOrNull(candidateIdHeader);
        log.info("REST: Fetch candidate result for assessment {}", aId);
        CandidateResultResponse response = evaluationService.getCandidateResult(candidateId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase D.2: Recruiter Detailed Assessment Report.
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<RecruiterReportResponse>> getRecruiterReport(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        log.info("REST: Fetch recruiter report for assessment {}", aId);
        RecruiterReportResponse response = evaluationService.getRecruiterReport(recruiterId, aId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Phase D.3: Recruiter Granular Test Results Breakdown.
     */
    @GetMapping("/test-results")
    public ResponseEntity<ApiResponse<List<RecruiterTestResultItemDto>>> getRecruiterTestResults(
            @PathVariable String assessmentId,
            @RequestHeader(value = "X-Recruiter-Id", required = false) String recruiterIdHeader) {
        UUID aId = UUIDUtils.parseUuidOrNull(assessmentId);
        if (aId == null) {
            throw new BadRequestException("Invalid assessment ID: must be a valid UUID");
        }
        UUID recruiterId = UUIDUtils.parseUuidOrNull(recruiterIdHeader);
        log.info("REST: Fetch recruiter test results for assessment {}", aId);
        List<RecruiterTestResultItemDto> results = evaluationService.getRecruiterTestResults(recruiterId, aId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
