package com.example.backend.assessment.entity;

import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
import com.example.backend.workspace.entity.Workspace;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "assessments")
public class Assessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "repository_url", nullable = false, length = 2048)
    private String repositoryUrl;

    @Column(name = "branch_name", nullable = false, length = 255)
    private String branchName;

    @Column(name = "backend_root_directory", nullable = false, length = 1000)
    private String backendRootDirectory;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 50)
    private Difficulty difficulty;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "scheduled_start_at", nullable = false)
    private Instant scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private Instant scheduledEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AssessmentStatus status = AssessmentStatus.CREATING;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    public Assessment() {
    }

    public Assessment(Workspace workspace, User candidate, String repositoryUrl, String branchName,
                      String backendRootDirectory, Difficulty difficulty, Integer durationMinutes,
                      Instant scheduledStartAt, Instant scheduledEndAt, AssessmentStatus status) {
        this.workspace = workspace;
        this.candidate = candidate;
        this.repositoryUrl = repositoryUrl;
        this.branchName = branchName;
        this.backendRootDirectory = backendRootDirectory;
        this.difficulty = difficulty;
        this.durationMinutes = durationMinutes;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.status = status != null ? status : AssessmentStatus.CREATING;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBackendRootDirectory() {
        return backendRootDirectory;
    }

    public void setBackendRootDirectory(String backendRootDirectory) {
        this.backendRootDirectory = backendRootDirectory;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Instant getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Instant scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Instant getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Instant scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
