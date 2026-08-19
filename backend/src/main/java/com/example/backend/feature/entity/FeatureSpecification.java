package com.example.backend.feature.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "feature_specifications")
public class FeatureSpecification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", nullable = false, columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "request_specification", columnDefinition = "TEXT")
    private String requestSpecification;

    @Column(name = "response_specification", columnDefinition = "TEXT")
    private String responseSpecification;

    @Column(name = "constraints", columnDefinition = "TEXT")
    private String constraints;

    public FeatureSpecification() {
    }

    public FeatureSpecification(Assessment assessment, String title, String description, String requirements,
                                String endpoint, String httpMethod, String requestSpecification,
                                String responseSpecification, String constraints) {
        this.assessment = assessment;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
        this.constraints = constraints;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
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
}
