package com.example.backend.assessment.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feature_specifications")
public class FeatureSpecification implements Persistable<UUID> {

    @Id
    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "feature_name", nullable = false, length = 255)
    private String featureName;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "request_specification", columnDefinition = "TEXT")
    private String requestSpecification;

    @Column(name = "response_specification", columnDefinition = "TEXT")
    private String responseSpecification;

    @Column(name = "constraints", columnDefinition = "TEXT")
    private String constraints;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private boolean isNewEntity = true;

    public FeatureSpecification() {
    }

    public FeatureSpecification(Assessment assessment, String featureName, String description, String requirements,
                                String requestSpecification, String responseSpecification, String constraints) {
        this(assessment, featureName, description, requirements, requestSpecification, responseSpecification, constraints, null, null);
    }

    public FeatureSpecification(Assessment assessment, String featureName, String description, String requirements,
                                String requestSpecification, String responseSpecification, String constraints,
                                String endpoint, String httpMethod) {
        this.assessment = assessment;
        this.assessmentId = assessment != null ? assessment.getId() : null;
        this.featureName = featureName;
        this.description = description;
        this.requirements = requirements;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
        this.constraints = constraints;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.assessmentId == null && assessment != null) {
            this.assessmentId = assessment.getId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    public UUID getId() {
        return assessmentId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
        if (assessment != null) {
            this.assessmentId = assessment.getId();
        }
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
