package com.example.backend.evaluation.dto;

import com.example.backend.common.enums.AssessmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CandidateResultResponse {

    private UUID assessmentId;
    private AssessmentStatus status;
    private BigDecimal score;
    private Integer testsPassed;
    private Integer totalTests;
    private Instant submittedAt;

    public CandidateResultResponse() {
    }

    public CandidateResultResponse(UUID assessmentId, AssessmentStatus status, BigDecimal score,
                                   Integer testsPassed, Integer totalTests, Instant submittedAt) {
        this.assessmentId = assessmentId;
        this.status = status;
        this.score = score;
        this.testsPassed = testsPassed;
        this.totalTests = totalTests;
        this.submittedAt = submittedAt;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getTestsPassed() {
        return testsPassed;
    }

    public void setTestsPassed(Integer testsPassed) {
        this.testsPassed = testsPassed;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
