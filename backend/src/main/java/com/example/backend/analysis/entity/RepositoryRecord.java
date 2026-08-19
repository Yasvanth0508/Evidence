package com.example.backend.analysis.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "repositories")
public class RepositoryRecord extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    @Column(name = "original_repository_path", nullable = false, length = 2048)
    private String originalRepositoryPath;

    @Column(name = "candidate_repository_path", length = 2048)
    private String candidateRepositoryPath;

    public RepositoryRecord() {
    }

    public RepositoryRecord(Assessment assessment, String originalRepositoryPath, String candidateRepositoryPath) {
        this.assessment = assessment;
        this.originalRepositoryPath = originalRepositoryPath;
        this.candidateRepositoryPath = candidateRepositoryPath;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getOriginalRepositoryPath() {
        return originalRepositoryPath;
    }

    public void setOriginalRepositoryPath(String originalRepositoryPath) {
        this.originalRepositoryPath = originalRepositoryPath;
    }

    public String getCandidateRepositoryPath() {
        return candidateRepositoryPath;
    }

    public void setCandidateRepositoryPath(String candidateRepositoryPath) {
        this.candidateRepositoryPath = candidateRepositoryPath;
    }
}
