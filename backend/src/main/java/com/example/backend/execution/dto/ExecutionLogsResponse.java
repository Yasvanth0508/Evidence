package com.example.backend.execution.dto;

public class ExecutionLogsResponse {

    private String logs;

    public ExecutionLogsResponse() {
    }

    public ExecutionLogsResponse(String logs) {
        this.logs = logs;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
