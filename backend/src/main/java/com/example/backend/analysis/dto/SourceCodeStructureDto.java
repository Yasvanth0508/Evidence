package com.example.backend.analysis.dto;

import java.util.List;

public class SourceCodeStructureDto {

    private List<String> controllers;
    private List<String> services;
    private List<String> repositories;
    private List<String> entities;

    public SourceCodeStructureDto() {
    }

    public SourceCodeStructureDto(List<String> controllers, List<String> services, List<String> repositories, List<String> entities) {
        this.controllers = controllers;
        this.services = services;
        this.repositories = repositories;
        this.entities = entities;
    }

    public List<String> getControllers() {
        return controllers;
    }

    public void setControllers(List<String> controllers) {
        this.controllers = controllers;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }
}
