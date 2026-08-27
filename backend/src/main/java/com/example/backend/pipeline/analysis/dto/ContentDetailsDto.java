package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentDetailsDto {
    private int totalControllers;
    private int totalEndpoints;
    private int totalEntities;
    private int totalRepositories;
    private int totalServices;
    private List<String> databaseTables = new ArrayList<>();
    private List<String> exposedRoutes = new ArrayList<>();
    private Map<String, Object> summaryMetrics = new HashMap<>();

    public ContentDetailsDto() {}

    public int getTotalControllers() { return totalControllers; }
    public void setTotalControllers(int totalControllers) { this.totalControllers = totalControllers; }

    public int getTotalEndpoints() { return totalEndpoints; }
    public void setTotalEndpoints(int totalEndpoints) { this.totalEndpoints = totalEndpoints; }

    public int getTotalEntities() { return totalEntities; }
    public void setTotalEntities(int totalEntities) { this.totalEntities = totalEntities; }

    public int getTotalRepositories() { return totalRepositories; }
    public void setTotalRepositories(int totalRepositories) { this.totalRepositories = totalRepositories; }

    public int getTotalServices() { return totalServices; }
    public void setTotalServices(int totalServices) { this.totalServices = totalServices; }

    public List<String> getDatabaseTables() { return databaseTables; }
    public void setDatabaseTables(List<String> databaseTables) { this.databaseTables = databaseTables; }

    public List<String> getExposedRoutes() { return exposedRoutes; }
    public void setExposedRoutes(List<String> exposedRoutes) { this.exposedRoutes = exposedRoutes; }

    public Map<String, Object> getSummaryMetrics() { return summaryMetrics; }
    public void setSummaryMetrics(Map<String, Object> summaryMetrics) { this.summaryMetrics = summaryMetrics; }
}
