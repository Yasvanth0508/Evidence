package com.example.backend.pipeline.feature.dto;

import java.util.UUID;

public class FeatureGenerationResult {

    private boolean success;
    private UUID assessmentId;
    private String featureName;
    private String endpoint;
    private String httpMethod;
    private String description;
    private String requirements;
    private String requestSpecification;
    private String responseSpecification;
    private String constraints;
    private String rawModelOutput;
    private String errorMessage;

    public FeatureGenerationResult() {
    }

    public static FeatureGenerationResult ok(UUID assessmentId, String featureName, String description,
                                             String requirements, String requestSpec, String responseSpec,
                                             String constraints, String rawOutput) {
        return ok(assessmentId, featureName, description, requirements, requestSpec, responseSpec, constraints, rawOutput, null, null);
    }

    public static FeatureGenerationResult ok(UUID assessmentId, String featureName, String description,
                                             String requirements, String requestSpec, String responseSpec,
                                             String constraints, String rawOutput, String endpoint, String httpMethod) {
        FeatureGenerationResult res = new FeatureGenerationResult();
        res.setSuccess(true);
        res.setAssessmentId(assessmentId);
        res.setFeatureName(featureName);
        res.setDescription(description);
        res.setRequirements(requirements);
        res.setRequestSpecification(requestSpec);
        res.setResponseSpecification(responseSpec);
        res.setConstraints(constraints);
        res.setRawModelOutput(rawOutput);
        res.setEndpoint(endpoint);
        res.setHttpMethod(httpMethod);
        return res;
    }

    public static FeatureGenerationResult fail(UUID assessmentId, String errorMessage) {
        FeatureGenerationResult res = new FeatureGenerationResult();
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

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getRequestSpecification() {
        return requestSpecification;
    }

    public void setRequestSpecification(String requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public String getResponseSpecification() {
        return responseSpecification;
    }

    public void setResponseSpecification(String responseSpecification) {
        this.responseSpecification = responseSpecification;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getRawModelOutput() {
        return rawModelOutput;
    }

    public void setRawModelOutput(String rawModelOutput) {
        this.rawModelOutput = rawModelOutput;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
