package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class ControllerInfo {
    private String className;
    private String packageName;
    private String basePath;
    private List<EndpointInfo> endpoints = new ArrayList<>();

    public ControllerInfo() {}

    public ControllerInfo(String className, String packageName, String basePath) {
        this.className = className;
        this.packageName = packageName;
        this.basePath = basePath;
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }

    public List<EndpointInfo> getEndpoints() { return endpoints; }
    public void setEndpoints(List<EndpointInfo> endpoints) { this.endpoints = endpoints; }
}
