package com.example.backend.evaluation.entity;

import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.TestResultStatus;
import com.example.backend.execution.entity.Execution;
import jakarta.persistence.*;

@Entity
@Table(name = "test_results")
public class TestResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    private Execution execution;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TestResultStatus status = TestResultStatus.PASSED;

    @Column(name = "actual_status_code")
    private Integer actualStatusCode;

    @Column(name = "actual_response", columnDefinition = "TEXT")
    private String actualResponse;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    public TestResult() {
    }

    public TestResult(TestCase testCase, Execution execution, TestResultStatus status,
                      Integer actualStatusCode, String actualResponse,
                      Integer executionTimeMs, String failureReason) {
        this.testCase = testCase;
        this.execution = execution;
        this.status = status != null ? status : TestResultStatus.PASSED;
        this.actualStatusCode = actualStatusCode;
        this.actualResponse = actualResponse;
        this.executionTimeMs = executionTimeMs;
        this.failureReason = failureReason;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public TestResultStatus getStatus() {
        return status;
    }

    public void setStatus(TestResultStatus status) {
        this.status = status;
    }

    public Integer getActualStatusCode() {
        return actualStatusCode;
    }

    public void setActualStatusCode(Integer actualStatusCode) {
        this.actualStatusCode = actualStatusCode;
    }

    public String getActualResponse() {
        return actualResponse;
    }

    public void setActualResponse(String actualResponse) {
        this.actualResponse = actualResponse;
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
