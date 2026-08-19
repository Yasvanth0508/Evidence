package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AssessmentListItemResponse {

    private UUID assessmentId;
    private UUID candidateId;
    private String candidateName;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private AssessmentStatus status;
    private BigDecimal score;

    public AssessmentListItemResponse() {
    }

    public AssessmentListItemResponse(UUID assessmentId, UUID candidateId, String candidateName,
                                      Difficulty difficulty, Integer durationMinutes,
                                      Instant scheduledStartAt, Instant scheduledEndAt,
                                      AssessmentStatus status, BigDecimal score) {
        this.assessmentId = assessmentId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.difficulty = difficulty;
        this.durationMinutes = durationMinutes;
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

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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
