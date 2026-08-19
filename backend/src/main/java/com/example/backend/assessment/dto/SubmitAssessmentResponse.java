package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;

import java.util.UUID;

public class SubmitAssessmentResponse {

    private UUID submissionId;
    private UUID assessmentId;
    private AssessmentStatus status;

    public SubmitAssessmentResponse() {
    }

    public SubmitAssessmentResponse(UUID submissionId, UUID assessmentId, AssessmentStatus status) {
        this.submissionId = submissionId;
        this.assessmentId = assessmentId;
        this.status = status;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
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
