package com.example.backend.assessment.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_workspaces")
public class AssessmentWorkspace implements Persistable<UUID> {

    @Id
    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "original_repository_path", nullable = false, columnDefinition = "TEXT")
    private String originalRepositoryPath;

    @Column(name = "candidate_workspace_path", columnDefinition = "TEXT")
    private String candidateWorkspacePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private boolean isNewEntity = true;

    public AssessmentWorkspace() {
    }

    public AssessmentWorkspace(Assessment assessment, String originalRepositoryPath, String candidateWorkspacePath) {
        this.assessment = assessment;
        this.assessmentId = assessment != null ? assessment.getId() : null;
        this.originalRepositoryPath = originalRepositoryPath;
        this.candidateWorkspacePath = candidateWorkspacePath;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.assessmentId == null && assessment != null) {
            this.assessmentId = assessment.getId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    public UUID getId() {
        return assessmentId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
        if (assessment != null) {
            this.assessmentId = assessment.getId();
        }
    }

    public String getOriginalRepositoryPath() {
        return originalRepositoryPath;
    }

    public void setOriginalRepositoryPath(String originalRepositoryPath) {
        this.originalRepositoryPath = originalRepositoryPath;
    }

    public String getCandidateWorkspacePath() {
        return candidateWorkspacePath;
    }

    public void setCandidateWorkspacePath(String candidateWorkspacePath) {
        this.candidateWorkspacePath = candidateWorkspacePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
