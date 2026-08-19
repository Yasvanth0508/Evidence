package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;

import java.util.List;
import java.util.UUID;

public class ProcessingStatusResponse {

    private UUID assessmentId;
    private AssessmentStatus status;
    private List<ProcessingStageDto> stages;

    public ProcessingStatusResponse() {
    }

    public ProcessingStatusResponse(UUID assessmentId, AssessmentStatus status, List<ProcessingStageDto> stages) {
        this.assessmentId = assessmentId;
        this.status = status;
        this.stages = stages;
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

    public List<ProcessingStageDto> getStages() {
        return stages;
    }

    public void setStages(List<ProcessingStageDto> stages) {
        this.stages = stages;
    }
}
