package com.example.backend.evaluation.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.SubmissionStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "candidate_repository_path", nullable = false, length = 2048)
    private String candidateRepositoryPath;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubmissionStatus status = SubmissionStatus.EVALUATING;

    public Submission() {
    }

    public Submission(Assessment assessment, String candidateRepositoryPath, Instant submittedAt, SubmissionStatus status) {
        this.assessment = assessment;
        this.candidateRepositoryPath = candidateRepositoryPath;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
        this.status = status != null ? status : SubmissionStatus.EVALUATING;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getCandidateRepositoryPath() {
        return candidateRepositoryPath;
    }

    public void setCandidateRepositoryPath(String candidateRepositoryPath) {
        this.candidateRepositoryPath = candidateRepositoryPath;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }
}
