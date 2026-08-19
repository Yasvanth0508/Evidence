package com.example.backend.selectedcandidate.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SelectCandidateRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    private UUID assessmentId;
    private String notes;

    public SelectCandidateRequest() {
    }

    public SelectCandidateRequest(UUID candidateId, UUID workspaceId, UUID assessmentId, String notes) {
        this.candidateId = candidateId;
        this.workspaceId = workspaceId;
        this.assessmentId = assessmentId;
        this.notes = notes;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
