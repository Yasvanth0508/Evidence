package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class SourceCodeStructureDto {
    private List<ControllerInfo> controllers = new ArrayList<>();
    private List<EntityInfo> entities = new ArrayList<>();
    private List<RepositoryInfo> repositories = new ArrayList<>();
    private List<ServiceInfo> services = new ArrayList<>();

    public SourceCodeStructureDto() {}

    public List<ControllerInfo> getControllers() { return controllers; }
    public void setControllers(List<ControllerInfo> controllers) { this.controllers = controllers; }

    public List<EntityInfo> getEntities() { return entities; }
    public void setEntities(List<EntityInfo> entities) { this.entities = entities; }

    public List<RepositoryInfo> getRepositories() { return repositories; }
    public void setRepositories(List<RepositoryInfo> repositories) { this.repositories = repositories; }

    public List<ServiceInfo> getServices() { return services; }
    public void setServices(List<ServiceInfo> services) { this.services = services; }
}
