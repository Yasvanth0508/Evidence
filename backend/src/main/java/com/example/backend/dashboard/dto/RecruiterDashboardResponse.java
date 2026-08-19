package com.example.backend.dashboard.dto;

public class RecruiterDashboardResponse {

    private long workspaceCount;
    private long candidateCount;
    private long assessmentCount;
    private long activeAssessments;
    private long completedAssessments;

    public RecruiterDashboardResponse() {
    }

    public RecruiterDashboardResponse(long workspaceCount, long candidateCount, long assessmentCount,
                                      long activeAssessments, long completedAssessments) {
        this.workspaceCount = workspaceCount;
        this.candidateCount = candidateCount;
        this.assessmentCount = assessmentCount;
        this.activeAssessments = activeAssessments;
        this.completedAssessments = completedAssessments;
    }

    public long getWorkspaceCount() {
        return workspaceCount;
    }

    public void setWorkspaceCount(long workspaceCount) {
        this.workspaceCount = workspaceCount;
    }

    public long getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(long candidateCount) {
        this.candidateCount = candidateCount;
    }

    public long getAssessmentCount() {
        return assessmentCount;
    }

    public void setAssessmentCount(long assessmentCount) {
        this.assessmentCount = assessmentCount;
    }

    public long getActiveAssessments() {
        return activeAssessments;
    }

    public void setActiveAssessments(long activeAssessments) {
        this.activeAssessments = activeAssessments;
    }

    public long getCompletedAssessments() {
        return completedAssessments;
    }

    public void setCompletedAssessments(long completedAssessments) {
        this.completedAssessments = completedAssessments;
    }
}
