package com.example.backend.feature.dto;

import java.util.List;

public class FeatureSpecificationResponse {

    private String title;
    private String description;
    private List<String> requirements;
    private String endpoint;
    private String httpMethod;
    private Object requestSpecification;
    private Object responseSpecification;
    private List<String> constraints;

    public FeatureSpecificationResponse() {
    }

    public FeatureSpecificationResponse(String title, String description, List<String> requirements,
                                        String endpoint, String httpMethod, Object requestSpecification,
                                        Object responseSpecification, List<String> constraints) {
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
        this.constraints = constraints;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
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

    public Object getRequestSpecification() {
        return requestSpecification;
    }

    public void setRequestSpecification(Object requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public Object getResponseSpecification() {
        return responseSpecification;
    }

    public void setResponseSpecification(Object responseSpecification) {
        this.responseSpecification = responseSpecification;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }
}
