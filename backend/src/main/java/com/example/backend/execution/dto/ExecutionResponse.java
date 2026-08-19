package com.example.backend.execution.dto;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;

import java.time.Instant;
import java.util.UUID;

public class ExecutionResponse {

    private UUID executionId;
    private BuildStatus buildStatus;
    private ContainerStatus containerStatus;
    private ApplicationStatus applicationStatus;
    private Instant startedAt;

    public ExecutionResponse() {
    }

    public ExecutionResponse(UUID executionId, BuildStatus buildStatus, ContainerStatus containerStatus,
                             ApplicationStatus applicationStatus, Instant startedAt) {
        this.executionId = executionId;
        this.buildStatus = buildStatus;
        this.containerStatus = containerStatus;
        this.applicationStatus = applicationStatus;
        this.startedAt = startedAt;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public void setExecutionId(UUID executionId) {
        this.executionId = executionId;
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
}
