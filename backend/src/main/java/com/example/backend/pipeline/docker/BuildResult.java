package com.example.backend.pipeline.docker;

public class BuildResult {
    private boolean success;
    private long durationMs;
    private String logs;
    private String errorMessage;

    public BuildResult(boolean success, long durationMs, String logs, String errorMessage) {
        this.success = success;
        this.durationMs = durationMs;
        this.logs = logs;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getLogs() {
        return logs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
