package com.example.backend.selectedcandidate.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.workspace.entity.Workspace;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "selected_candidates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_selected_candidate", columnNames = {"workspace_id", "candidate_id"})
    }
)
public class SelectedCandidate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(name = "selection_notes", columnDefinition = "TEXT")
    private String selectionNotes;

    @Column(name = "selected_at", nullable = false)
    private Instant selectedAt = Instant.now();

    public SelectedCandidate() {
    }

    public SelectedCandidate(User candidate, Workspace workspace, Assessment assessment, User recruiter,
                             String selectionNotes, Instant selectedAt) {
        this.candidate = candidate;
        this.workspace = workspace;
        this.assessment = assessment;
        this.recruiter = recruiter;
        this.selectionNotes = selectionNotes;
        this.selectedAt = selectedAt != null ? selectedAt : Instant.now();
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
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
