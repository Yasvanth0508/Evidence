package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;

import java.util.UUID;

public class StartAssessmentResponse {

    private UUID assessmentId;
    private AssessmentStatus status;

    public StartAssessmentResponse() {
    }

    public StartAssessmentResponse(UUID assessmentId, AssessmentStatus status) {
        this.assessmentId = assessmentId;
        this.status = status;
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
}
