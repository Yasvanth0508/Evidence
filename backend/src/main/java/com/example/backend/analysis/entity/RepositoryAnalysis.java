package com.example.backend.analysis.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.AnalysisStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "repository_analyses")
public class RepositoryAnalysis extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 50)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "repository_structure", columnDefinition = "TEXT")
    private String repositoryStructure;

    @Column(name = "controllers", columnDefinition = "TEXT")
    private String controllers;

    @Column(name = "services", columnDefinition = "TEXT")
    private String services;

    @Column(name = "repositories", columnDefinition = "TEXT")
    private String repositories;

    @Column(name = "entities", columnDefinition = "TEXT")
    private String entities;

    @Column(name = "endpoints", columnDefinition = "TEXT")
    private String endpoints;

    @Column(name = "entity_fields", columnDefinition = "TEXT")
    private String entityFields;

    @Column(name = "service_methods", columnDefinition = "TEXT")
    private String serviceMethods;

    public RepositoryAnalysis() {
    }

    public RepositoryAnalysis(Assessment assessment, AnalysisStatus analysisStatus) {
        this.assessment = assessment;
        this.analysisStatus = analysisStatus != null ? analysisStatus : AnalysisStatus.PENDING;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public String getRepositoryStructure() {
        return repositoryStructure;
    }

    public void setRepositoryStructure(String repositoryStructure) {
        this.repositoryStructure = repositoryStructure;
    }

    public String getControllers() {
        return controllers;
    }

    public void setControllers(String controllers) {
        this.controllers = controllers;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getRepositories() {
        return repositories;
    }

    public void setRepositories(String repositories) {
        this.repositories = repositories;
    }

    public String getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public String getEntityFields() {
        return entityFields;
    }

    public void setEntityFields(String entityFields) {
        this.entityFields = entityFields;
    }

    public String getServiceMethods() {
        return serviceMethods;
    }

    public void setServiceMethods(String serviceMethods) {
        this.serviceMethods = serviceMethods;
    }
}
