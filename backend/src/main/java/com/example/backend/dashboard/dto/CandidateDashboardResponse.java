package com.example.backend.dashboard.dto;

import java.util.List;

public class CandidateDashboardResponse {

    private List<ScheduledAssessmentDto> scheduledAssessments;
    private List<CompletedAssessmentDto> completedAssessments;

    public CandidateDashboardResponse() {
    }

    public CandidateDashboardResponse(List<ScheduledAssessmentDto> scheduledAssessments,
                                      List<CompletedAssessmentDto> completedAssessments) {
        this.scheduledAssessments = scheduledAssessments;
        this.completedAssessments = completedAssessments;
    }

    public List<ScheduledAssessmentDto> getScheduledAssessments() {
        return scheduledAssessments;
    }

    public void setScheduledAssessments(List<ScheduledAssessmentDto> scheduledAssessments) {
        this.scheduledAssessments = scheduledAssessments;
    }

    public List<CompletedAssessmentDto> getCompletedAssessments() {
        return completedAssessments;
    }

    public void setCompletedAssessments(List<CompletedAssessmentDto> completedAssessments) {
        this.completedAssessments = completedAssessments;
    }
}
