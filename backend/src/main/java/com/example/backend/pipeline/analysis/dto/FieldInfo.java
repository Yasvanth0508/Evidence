package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class FieldInfo {
    private String name;
    private String type;
    private boolean isId;
    private String relation; // OneToMany, ManyToOne, OneToOne, ManyToMany
    private String targetEntity;
    private List<String> annotations = new ArrayList<>();

    public FieldInfo() {}

    public FieldInfo(String name, String type, boolean isId) {
        this.name = name;
        this.type = type;
        this.isId = isId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isId() { return isId; }
    public void setId(boolean id) { isId = id; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }

    public List<String> getAnnotations() { return annotations; }
    public void setAnnotations(List<String> annotations) { this.annotations = annotations; }
}
