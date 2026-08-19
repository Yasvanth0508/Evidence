package com.example.backend.evaluation.dto;

import com.example.backend.common.enums.TestResultStatus;

public class TestResultItemDto {

    private Integer testCaseNumber;
    private String endpoint;
    private String httpMethod;
    private TestResultStatus status;
    private Integer expectedStatusCode;
    private Integer actualStatusCode;
    private Integer executionTimeMs;
    private String failureReason;

    public TestResultItemDto() {
    }

    public TestResultItemDto(Integer testCaseNumber, String endpoint, String httpMethod, TestResultStatus status,
                             Integer expectedStatusCode, Integer actualStatusCode, Integer executionTimeMs, String failureReason) {
        this.testCaseNumber = testCaseNumber;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.status = status;
        this.expectedStatusCode = expectedStatusCode;
        this.actualStatusCode = actualStatusCode;
        this.executionTimeMs = executionTimeMs;
        this.failureReason = failureReason;
    }

    public Integer getTestCaseNumber() {
        return testCaseNumber;
    }

    public void setTestCaseNumber(Integer testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public TestResultStatus getStatus() {
        return status;
    }

    public void setStatus(TestResultStatus status) {
        this.status = status;
    }

    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(Integer expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public Integer getActualStatusCode() {
        return actualStatusCode;
    }

    public void setActualStatusCode(Integer actualStatusCode) {
        this.actualStatusCode = actualStatusCode;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
