package com.example.backend.assessment.entity;

import com.example.backend.common.enums.SubmissionStatus;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class Submission implements Persistable<UUID> {

    @Id
    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "time_taken_seconds", nullable = false)
    private Long timeTakenSeconds = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Transient
    private boolean isNewEntity = true;

    public Submission() {
    }

    public Submission(Assessment assessment, Instant submittedAt, Long timeTakenSeconds, SubmissionStatus status) {
        this.assessment = assessment;
        this.assessmentId = assessment != null ? assessment.getId() : null;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
        this.timeTakenSeconds = timeTakenSeconds != null ? timeTakenSeconds : 0L;
        this.status = status != null ? status : SubmissionStatus.SUBMITTED;
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) {
            this.submittedAt = Instant.now();
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

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Long timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }
}
