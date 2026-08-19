package com.example.backend.selectedcandidate.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SelectedCandidateItemDto {

    private UUID id;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private UUID workspaceId;
    private String workspaceName;
    private UUID assessmentId;
    private BigDecimal score;
    private String selectionNotes;
    private Instant selectedAt;

    public SelectedCandidateItemDto() {
    }

    public SelectedCandidateItemDto(UUID id, UUID candidateId, String candidateName, String candidateEmail,
                                    UUID workspaceId, String workspaceName, UUID assessmentId,
                                    BigDecimal score, String selectionNotes, Instant selectedAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.assessmentId = assessmentId;
        this.score = score;
        this.selectionNotes = selectionNotes;
        this.selectedAt = selectedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getSelectionNotes() {
        return selectionNotes;
    }

    public void setSelectionNotes(String selectionNotes) {
        this.selectionNotes = selectionNotes;
    }

    public Instant getSelectedAt() {
        return selectedAt;
    }

    public void setSelectedAt(Instant selectedAt) {
        this.selectedAt = selectedAt;
    }
}
