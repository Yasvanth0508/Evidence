package com.example.backend.pipeline.testcase.dto;

import com.example.backend.common.enums.TestType;

import java.math.BigDecimal;

public class TestCaseItemDto {

    private int testCaseNumber;
    private TestType testType = TestType.BUSINESS_LOGIC;
    private String httpMethod;
    private String endpoint;
    private String requestData;
    private int expectedStatusCode;
    private String expectedResponse;
    private String assertions;
    private BigDecimal weight = BigDecimal.valueOf(1.00);
    private String description;

    public TestCaseItemDto() {
    }

    public TestCaseItemDto(int testCaseNumber, TestType testType, String httpMethod, String endpoint,
                           String requestData, int expectedStatusCode, String expectedResponse,
                           String assertions, BigDecimal weight, String description) {
        this.testCaseNumber = testCaseNumber;
        this.testType = testType != null ? testType : TestType.BUSINESS_LOGIC;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.requestData = requestData;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponse = expectedResponse;
        this.assertions = assertions != null ? assertions : "[]";
        this.weight = weight != null ? weight : BigDecimal.valueOf(1.00);
        this.description = description;
    }

    public int getTestCaseNumber() {
        return testCaseNumber;
    }

    public void setTestCaseNumber(int testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public String getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    public String getAssertions() {
        return assertions;
    }

    public void setAssertions(String assertions) {
        this.assertions = assertions;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
