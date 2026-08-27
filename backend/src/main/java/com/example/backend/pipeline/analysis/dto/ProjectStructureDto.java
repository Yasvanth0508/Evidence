package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class ProjectStructureDto {
    private String language = "Java";
    private String javaVersion = "21";
    private String buildTool = "Maven";
    private String springBootVersion;
    private int totalJavaFiles;
    private List<String> dependencies = new ArrayList<>();
    private List<String> packages = new ArrayList<>();
    private List<String> topLevelFiles = new ArrayList<>();

    public ProjectStructureDto() {}

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

    public String getBuildTool() { return buildTool; }
    public void setBuildTool(String buildTool) { this.buildTool = buildTool; }

    public String getSpringBootVersion() { return springBootVersion; }
    public void setSpringBootVersion(String springBootVersion) { this.springBootVersion = springBootVersion; }

    public int getTotalJavaFiles() { return totalJavaFiles; }
    public void setTotalJavaFiles(int totalJavaFiles) { this.totalJavaFiles = totalJavaFiles; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public List<String> getPackages() { return packages; }
    public void setPackages(List<String> packages) { this.packages = packages; }

    public List<String> getTopLevelFiles() { return topLevelFiles; }
    public void setTopLevelFiles(List<String> topLevelFiles) { this.topLevelFiles = topLevelFiles; }
}
