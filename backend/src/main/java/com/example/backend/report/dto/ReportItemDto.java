package com.example.backend.report.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ReportItemDto {

    private UUID reportId;
    private String candidateName;
    private String candidateEmail;
    private String workspaceName;
    private Difficulty difficulty;
    private BigDecimal score;
    private AssessmentStatus status;
    private Instant completedAt;

    public ReportItemDto() {
    }

    public ReportItemDto(UUID reportId, String candidateName, String candidateEmail,
                         String workspaceName, Difficulty difficulty, BigDecimal score,
                         AssessmentStatus status, Instant completedAt) {
        this.reportId = reportId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.workspaceName = workspaceName;
        this.difficulty = difficulty;
        this.score = score;
        this.status = status;
        this.completedAt = completedAt;
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
