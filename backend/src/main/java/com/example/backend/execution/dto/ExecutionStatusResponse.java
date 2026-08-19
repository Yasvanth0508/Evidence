package com.example.backend.execution.dto;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;

import java.time.Instant;

public class ExecutionStatusResponse {

    private BuildStatus buildStatus;
    private ContainerStatus containerStatus;
    private ApplicationStatus applicationStatus;
    private Instant startedAt;
    private Instant stoppedAt;

    public ExecutionStatusResponse() {
    }

    public ExecutionStatusResponse(BuildStatus buildStatus, ContainerStatus containerStatus,
                                   ApplicationStatus applicationStatus, Instant startedAt, Instant stoppedAt) {
        this.buildStatus = buildStatus;
        this.containerStatus = containerStatus;
        this.applicationStatus = applicationStatus;
        this.startedAt = startedAt;
        this.stoppedAt = stoppedAt;
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
