package com.example.backend.evaluation.dto;

import java.util.List;
import java.util.UUID;

public class TestResultsListResponse {

    private UUID assessmentId;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private List<TestResultItemDto> results;

    public TestResultsListResponse() {
    }

    public TestResultsListResponse(UUID assessmentId, Integer totalTests, Integer passedTests,
                                   Integer failedTests, List<TestResultItemDto> results) {
        this.assessmentId = assessmentId;
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.results = results;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
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

    public List<TestResultItemDto> getResults() {
        return results;
    }

    public void setResults(List<TestResultItemDto> results) {
        this.results = results;
    }
}
