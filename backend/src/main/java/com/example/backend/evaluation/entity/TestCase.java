package com.example.backend.evaluation.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(
    name = "test_cases",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_assessment_testcase", columnNames = {"assessment_id", "test_case_number"})
    }
)
public class TestCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "test_case_number", nullable = false)
    private Integer testCaseNumber;

    @Column(name = "http_method", nullable = false, length = 20)
    private String httpMethod;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "expected_status_code", nullable = false)
    private Integer expectedStatusCode;

    @Column(name = "expected_response", columnDefinition = "TEXT")
    private String expectedResponse;

    @Column(name = "assertions", columnDefinition = "TEXT")
    private String assertions;

    public TestCase() {
    }

    public TestCase(Assessment assessment, Integer testCaseNumber, String httpMethod, String endpoint,
                    String requestData, Integer expectedStatusCode, String expectedResponse, String assertions) {
        this.assessment = assessment;
        this.testCaseNumber = testCaseNumber;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.requestData = requestData;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedResponse = expectedResponse;
        this.assertions = assertions;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Integer getTestCaseNumber() {
        return testCaseNumber;
    }

    public void setTestCaseNumber(Integer testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
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

    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(Integer expectedStatusCode) {
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
}
