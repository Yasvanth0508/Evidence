package com.example.backend.pipeline.docker.dto;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;

public class DockerValidationResult {

    private boolean success;
    private BuildStatus buildStatus;
    private ApplicationStatus applicationStatus;
    private String imageTag;
    private String containerId;
    private int exposedPort;
    private long buildDurationMs;
    private long startupDurationMs;
    private String buildLogs;
    private String startupLogs;
    private String failureReason;

    public DockerValidationResult() {
    }

    public DockerValidationResult(boolean success, BuildStatus buildStatus, ApplicationStatus applicationStatus,
                                  String imageTag, String containerId, int exposedPort,
                                  long buildDurationMs, long startupDurationMs,
                                  String buildLogs, String startupLogs, String failureReason) {
        this.success = success;
        this.buildStatus = buildStatus;
        this.applicationStatus = applicationStatus;
        this.imageTag = imageTag;
        this.containerId = containerId;
        this.exposedPort = exposedPort;
        this.buildDurationMs = buildDurationMs;
        this.startupDurationMs = startupDurationMs;
        this.buildLogs = buildLogs;
        this.startupLogs = startupLogs;
        this.failureReason = failureReason;
    }

    public static DockerValidationResult ok(String imageTag, String containerId, int port,
                                            long buildMs, long startMs, String buildLogs, String startupLogs) {
        return new DockerValidationResult(true, BuildStatus.SUCCESS, ApplicationStatus.STARTED,
                imageTag, containerId, port, buildMs, startMs, buildLogs, startupLogs, null);
    }

    public static DockerValidationResult fail(BuildStatus buildStatus, ApplicationStatus appStatus,
                                              String buildLogs, String startupLogs, String failureReason) {
        return new DockerValidationResult(false, buildStatus, appStatus,
                null, null, 0, 0L, 0L, buildLogs, startupLogs, failureReason);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public int getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(int exposedPort) {
        this.exposedPort = exposedPort;
    }

    public long getBuildDurationMs() {
        return buildDurationMs;
    }

    public void setBuildDurationMs(long buildDurationMs) {
        this.buildDurationMs = buildDurationMs;
    }

    public long getStartupDurationMs() {
        return startupDurationMs;
    }

    public void setStartupDurationMs(long startupDurationMs) {
        this.startupDurationMs = startupDurationMs;
    }

    public String getBuildLogs() {
        return buildLogs;
    }

    public void setBuildLogs(String buildLogs) {
        this.buildLogs = buildLogs;
    }

    public String getStartupLogs() {
        return startupLogs;
    }

    public void setStartupLogs(String startupLogs) {
        this.startupLogs = startupLogs;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
