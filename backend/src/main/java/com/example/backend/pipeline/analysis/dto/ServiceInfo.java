package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
    private String className;
    private String packageName;
    private List<String> methods = new ArrayList<>();

    public ServiceInfo() {}

    public ServiceInfo(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public List<String> getMethods() { return methods; }
    public void setMethods(List<String> methods) { this.methods = methods; }
}
