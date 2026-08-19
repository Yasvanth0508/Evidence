package com.example.backend.evaluation.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.TestResultStatus;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.evaluation.dto.AssessmentReportResponse;
import com.example.backend.evaluation.dto.CandidateResultResponse;
import com.example.backend.evaluation.dto.TestResultItemDto;
import com.example.backend.evaluation.dto.TestResultsListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EvaluationService {

    private final AssessmentRepository assessmentRepository;

    public EvaluationService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public CandidateResultResponse getCandidateResult(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        BigDecimal score = assessment.getScore() != null ? assessment.getScore() : BigDecimal.valueOf(85.00);

        return new CandidateResultResponse(
                assessment.getId(),
                assessment.getStatus() != null ? assessment.getStatus() : AssessmentStatus.COMPLETED,
                score,
                4,
                5,
                assessment.getUpdatedAt() != null ? assessment.getUpdatedAt() : Instant.now()
        );
    }

    public AssessmentReportResponse getAssessmentReport(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        BigDecimal score = assessment.getScore() != null ? assessment.getScore() : BigDecimal.valueOf(85.00);

        return new AssessmentReportResponse(
                assessment.getId(),
                assessment.getCandidate().getName(),
                assessment.getCandidate().getEmail(),
                assessment.getWorkspace().getName(),
                assessment.getDifficulty(),
                score,
                assessment.getStatus() != null ? assessment.getStatus() : AssessmentStatus.COMPLETED,
                4,
                5,
                assessment.getDurationMinutes(),
                assessment.getUpdatedAt() != null ? assessment.getUpdatedAt() : Instant.now()
        );
    }

    public TestResultsListResponse getTestResults(UUID assessmentId) {
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        List<TestResultItemDto> results = Arrays.asList(
                new TestResultItemDto(
                        1,
                        "/api/notes/search?keyword=Meeting",
                        "GET",
                        TestResultStatus.PASSED,
                        200,
                        200,
                        42,
                        null
                ),
                new TestResultItemDto(
                        2,
                        "/api/notes/search?keyword=nonexistent",
                        "GET",
                        TestResultStatus.PASSED,
                        200,
                        200,
                        28,
                        null
                ),
                new TestResultItemDto(
                        3,
                        "/api/notes/search",
                        "GET",
                        TestResultStatus.PASSED,
                        400,
                        400,
                        15,
                        null
                ),
                new TestResultItemDto(
                        4,
                        "/api/notes/search?keyword=spring",
                        "GET",
                        TestResultStatus.PASSED,
                        200,
                        200,
                        35,
                        null
                ),
                new TestResultItemDto(
                        5,
                        "/api/notes/search?keyword=CASE_INSENSITIVE",
                        "GET",
                        TestResultStatus.FAILED,
                        200,
                        404,
                        50,
                        "Expected 200 OK with matching notes, but received 404 Not Found"
                )
        );

        return new TestResultsListResponse(
                assessmentId,
                5,
                4,
                1,
                results
        );
    }
}
