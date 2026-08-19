package com.example.backend.dashboard.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;

import java.time.Instant;
import java.util.UUID;

public class ScheduledAssessmentDto {

    private UUID assessmentId;
    private String workspaceName;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private Difficulty difficulty;
    private AssessmentStatus status;

    public ScheduledAssessmentDto() {
    }

    public ScheduledAssessmentDto(UUID assessmentId, String workspaceName, Instant scheduledStartAt,
                                  Instant scheduledEndAt, Difficulty difficulty, AssessmentStatus status) {
        this.assessmentId = assessmentId;
        this.workspaceName = workspaceName;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.difficulty = difficulty;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
}
