package com.example.backend.evaluation.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AssessmentReportResponse {

    private UUID assessmentId;
    private String candidateName;
    private String candidateEmail;
    private String workspaceName;
    private Difficulty difficulty;
    private BigDecimal score;
    private AssessmentStatus status;
    private Integer testsPassed;
    private Integer totalTests;
    private Integer durationMinutes;
    private Instant completedAt;

    public AssessmentReportResponse() {
    }

    public AssessmentReportResponse(UUID assessmentId, String candidateName, String candidateEmail,
                                    String workspaceName, Difficulty difficulty, BigDecimal score,
                                    AssessmentStatus status, Integer testsPassed, Integer totalTests,
                                    Integer durationMinutes, Instant completedAt) {
        this.assessmentId = assessmentId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.workspaceName = workspaceName;
        this.difficulty = difficulty;
        this.score = score;
        this.status = status;
        this.testsPassed = testsPassed;
        this.totalTests = totalTests;
        this.durationMinutes = durationMinutes;
        this.completedAt = completedAt;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
