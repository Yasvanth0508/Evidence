package com.example.backend.candidate.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CandidateAssessmentDto {

    private UUID assessmentId;
    private Difficulty difficulty;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private AssessmentStatus status;
    private BigDecimal score;

    public CandidateAssessmentDto() {
    }

    public CandidateAssessmentDto(UUID assessmentId, Difficulty difficulty, Instant scheduledStartAt, Instant scheduledEndAt, AssessmentStatus status, BigDecimal score) {
        this.assessmentId = assessmentId;
        this.difficulty = difficulty;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.status = status;
        this.score = score;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Instant getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Instant scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Instant getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Instant scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
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
}
