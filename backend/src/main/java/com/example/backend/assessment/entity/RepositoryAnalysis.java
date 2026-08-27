package com.example.backend.assessment.entity;

import com.example.backend.common.enums.AnalysisStatus;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repository_analyses")
public class RepositoryAnalysis implements Persistable<UUID> {

    @Id
    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "project_structure", columnDefinition = "TEXT")
    private String projectStructure;

    @Column(name = "source_code_structure", columnDefinition = "TEXT")
    private String sourceCodeStructure;

    @Column(name = "content_details", columnDefinition = "TEXT")
    private String contentDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 50)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Transient
    private boolean isNewEntity = true;

    public RepositoryAnalysis() {
    }

    public RepositoryAnalysis(Assessment assessment, AnalysisStatus analysisStatus) {
        this.assessment = assessment;
        this.assessmentId = assessment != null ? assessment.getId() : null;
        this.analysisStatus = analysisStatus != null ? analysisStatus : AnalysisStatus.PENDING;
        this.createdAt = Instant.now();
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.assessmentId == null && assessment != null) {
            this.assessmentId = assessment.getId();
        }
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

    public String getProjectStructure() {
        return projectStructure;
    }

    public void setProjectStructure(String projectStructure) {
        this.projectStructure = projectStructure;
    }

    public String getSourceCodeStructure() {
        return sourceCodeStructure;
    }

    public void setSourceCodeStructure(String sourceCodeStructure) {
        this.sourceCodeStructure = sourceCodeStructure;
    }

    public String getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(String contentDetails) {
        this.contentDetails = contentDetails;
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
