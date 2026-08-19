package com.example.backend.dashboard.dto;

import com.example.backend.common.enums.AssessmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CompletedAssessmentDto {

    private UUID assessmentId;
    private String workspaceName;
    private Instant completedAt;
    private BigDecimal score;
    private AssessmentStatus status;

    public CompletedAssessmentDto() {
    }

    public CompletedAssessmentDto(UUID assessmentId, String workspaceName, Instant completedAt,
                                  BigDecimal score, AssessmentStatus status) {
        this.assessmentId = assessmentId;
        this.workspaceName = workspaceName;
        this.completedAt = completedAt;
        this.score = score;
        this.status = status;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
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
}
