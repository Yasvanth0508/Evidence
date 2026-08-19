package com.example.backend.analysis.dto;

public class AnalysisStatusResponse {

    private String status;

    public AnalysisStatusResponse() {
    }

    public AnalysisStatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
