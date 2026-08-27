package com.example.backend.pipeline.analysis.dto;

public class ParameterInfo {
    private String name;
    private String type;
    private String annotation; // PathVariable, RequestParam, RequestBody, etc.
    private boolean required;

    public ParameterInfo() {}

    public ParameterInfo(String name, String type, String annotation, boolean required) {
        this.name = name;
        this.type = type;
        this.annotation = annotation;
        this.required = required;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAnnotation() { return annotation; }
    public void setAnnotation(String annotation) { this.annotation = annotation; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
