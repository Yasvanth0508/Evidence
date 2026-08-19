package com.example.backend.execution.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "executions")
public class Execution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "container_id", length = 255)
    private String containerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "build_status", nullable = false, length = 50)
    private BuildStatus buildStatus = BuildStatus.SUCCESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "container_status", nullable = false, length = 50)
    private ContainerStatus containerStatus = ContainerStatus.STOPPED;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 50)
    private ApplicationStatus applicationStatus = ApplicationStatus.STARTED;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    public Execution() {
    }

    public Execution(Assessment assessment, String containerId, BuildStatus buildStatus,
                     ContainerStatus containerStatus, ApplicationStatus applicationStatus,
                     Instant startedAt, Instant stoppedAt) {
        this.assessment = assessment;
        this.containerId = containerId;
        this.buildStatus = buildStatus != null ? buildStatus : BuildStatus.SUCCESS;
        this.containerStatus = containerStatus != null ? containerStatus : ContainerStatus.STOPPED;
        this.applicationStatus = applicationStatus != null ? applicationStatus : ApplicationStatus.STARTED;
        this.startedAt = startedAt;
        this.stoppedAt = stoppedAt;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public ContainerStatus getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(ContainerStatus containerStatus) {
        this.containerStatus = containerStatus;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
    }
}
