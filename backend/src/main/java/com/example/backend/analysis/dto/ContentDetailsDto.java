package com.example.backend.analysis.dto;

import java.util.List;
import java.util.Map;

public class ContentDetailsDto {

    private List<Map<String, String>> endpoints;
    private List<Map<String, String>> entityFields;
    private List<Map<String, String>> serviceMethods;

    public ContentDetailsDto() {
    }

    public ContentDetailsDto(List<Map<String, String>> endpoints,
                             List<Map<String, String>> entityFields,
                             List<Map<String, String>> serviceMethods) {
        this.endpoints = endpoints;
        this.entityFields = entityFields;
        this.serviceMethods = serviceMethods;
    }

    public List<Map<String, String>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Map<String, String>> endpoints) {
        this.endpoints = endpoints;
    }

    public List<Map<String, String>> getEntityFields() {
        return entityFields;
    }

    public void setEntityFields(List<Map<String, String>> entityFields) {
        this.entityFields = entityFields;
    }

    public List<Map<String, String>> getServiceMethods() {
        return serviceMethods;
    }

    public void setServiceMethods(List<Map<String, String>> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }
}
