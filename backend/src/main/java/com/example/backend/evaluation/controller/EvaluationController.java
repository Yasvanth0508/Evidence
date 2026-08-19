package com.example.backend.evaluation.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.evaluation.dto.AssessmentReportResponse;
import com.example.backend.evaluation.dto.CandidateResultResponse;
import com.example.backend.evaluation.dto.TestResultsListResponse;
import com.example.backend.evaluation.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<CandidateResultResponse>> getCandidateResult(@PathVariable UUID assessmentId) {
        CandidateResultResponse response = evaluationService.getCandidateResult(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Candidate result retrieved successfully", response));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<AssessmentReportResponse>> getAssessmentReport(@PathVariable UUID assessmentId) {
        AssessmentReportResponse response = evaluationService.getAssessmentReport(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment report retrieved successfully", response));
    }

    @GetMapping("/test-results")
    public ResponseEntity<ApiResponse<TestResultsListResponse>> getTestResults(@PathVariable UUID assessmentId) {
        TestResultsListResponse response = evaluationService.getTestResults(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Test results retrieved successfully", response));
    }
}
