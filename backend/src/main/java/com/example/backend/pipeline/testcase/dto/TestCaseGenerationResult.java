package com.example.backend.pipeline.testcase.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestCaseGenerationResult {

    private boolean success;
    private UUID assessmentId;
    private List<TestCaseItemDto> testCases = new ArrayList<>();
    private int totalTestCases;
    private String rawModelOutput;
    private String errorMessage;

    public TestCaseGenerationResult() {
    }

    public static TestCaseGenerationResult ok(UUID assessmentId, List<TestCaseItemDto> testCases, String rawOutput) {
        TestCaseGenerationResult res = new TestCaseGenerationResult();
        res.setSuccess(true);
        res.setAssessmentId(assessmentId);
        res.setTestCases(testCases != null ? testCases : new ArrayList<>());
        res.setTotalTestCases(res.getTestCases().size());
        res.setRawModelOutput(rawOutput);
        return res;
    }

    public static TestCaseGenerationResult fail(UUID assessmentId, String errorMessage) {
        TestCaseGenerationResult res = new TestCaseGenerationResult();
        res.setSuccess(false);
        res.setAssessmentId(assessmentId);
        res.setErrorMessage(errorMessage);
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<TestCaseItemDto> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseItemDto> testCases) {
        this.testCases = testCases;
        this.totalTestCases = testCases != null ? testCases.size() : 0;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public String getRawModelOutput() {
        return rawModelOutput;
    }

    public void setRawModelOutput(String rawModelOutput) {
        this.rawModelOutput = rawModelOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
