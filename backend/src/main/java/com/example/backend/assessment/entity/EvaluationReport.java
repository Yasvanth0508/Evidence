package com.example.backend.assessment.entity;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evaluation_reports")
@Getter
@Setter
public class EvaluationReport implements Persistable<UUID> {

    @Id
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "score", precision = 5, scale = 2, nullable = false)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "score_rating", length = 50)
    private String scoreRating = "Needs Improvement";

    @Column(name = "total_tests", nullable = false)
    private Integer totalTests = 0;

    @Column(name = "passed_tests", nullable = false)
    private Integer passedTests = 0;

    @Column(name = "failed_tests", nullable = false)
    private Integer failedTests = 0;

    @Column(name = "business_logic_total")
    private Integer businessLogicTotal = 0;

    @Column(name = "business_logic_passed")
    private Integer businessLogicPassed = 0;

    @Column(name = "syntax_total")
    private Integer syntaxTotal = 0;

    @Column(name = "syntax_passed")
    private Integer syntaxPassed = 0;

    @Column(name = "data_flow_total")
    private Integer dataFlowTotal = 0;

    @Column(name = "data_flow_passed")
    private Integer dataFlowPassed = 0;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "strengths_json", columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(name = "improvements_json", columnDefinition = "TEXT")
    private String improvementsJson;

    @Column(name = "copy_paste_events")
    private Integer copyPasteEvents = 0;

    @Column(name = "tab_switch_count")
    private Integer tabSwitchCount = 0;

    @Column(name = "build_runs")
    private Integer buildRuns = 1;

    @Column(name = "risk_analysis", columnDefinition = "TEXT")
    private String riskAnalysis;

    @Column(name = "overall_risk_badge", length = 20)
    private String overallRiskBadge = "LOW";

    @Enumerated(EnumType.STRING)
    @Column(name = "build_status", nullable = false, length = 50)
    private BuildStatus buildStatus = BuildStatus.SUCCESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 50)
    private ApplicationStatus applicationStatus = ApplicationStatus.STARTED;

    @Column(name = "time_taken_seconds", nullable = false)
    private Long timeTakenSeconds = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubmissionStatus status = SubmissionStatus.COMPLETED;

    @Column(name = "evaluated_at")
    private Instant evaluatedAt;

    @Transient
    private boolean isNewEntity = true;

    public EvaluationReport() {
    }

    public EvaluationReport(Submission submission, BigDecimal score, Integer totalTests, Integer passedTests,
                            Integer failedTests, BuildStatus buildStatus, ApplicationStatus applicationStatus,
                            Long timeTakenSeconds, SubmissionStatus status, Instant evaluatedAt) {
        this.submission = submission;
        this.submissionId = submission != null ? submission.getId() : null;
        this.score = score != null ? score : BigDecimal.ZERO;
        this.totalTests = totalTests != null ? totalTests : 0;
        this.passedTests = passedTests != null ? passedTests : 0;
        this.failedTests = failedTests != null ? failedTests : 0;
        this.buildStatus = buildStatus != null ? buildStatus : BuildStatus.SUCCESS;
        this.applicationStatus = applicationStatus != null ? applicationStatus : ApplicationStatus.STARTED;
        this.timeTakenSeconds = timeTakenSeconds != null ? timeTakenSeconds : 0L;
        this.status = status != null ? status : SubmissionStatus.COMPLETED;
        this.evaluatedAt = evaluatedAt != null ? evaluatedAt : Instant.now();
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.evaluatedAt == null) {
            this.evaluatedAt = Instant.now();
        }
        if (this.submissionId == null && submission != null) {
            this.submissionId = submission.getId();
        }
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    public UUID getId() {
        return submissionId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
        if (submission != null) {
            this.submissionId = submission.getId();
        }
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }

    public Integer getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }

    public Integer getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(Integer failedTests) {
        this.failedTests = failedTests;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
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

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
