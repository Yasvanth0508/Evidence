package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
    private String className;
    private String tableName;
    private String packageName;
    private List<FieldInfo> fields = new ArrayList<>();
    private List<String> relations = new ArrayList<>();

    public EntityInfo() {}

    public EntityInfo(String className, String tableName, String packageName) {
        this.className = className;
        this.tableName = tableName;
        this.packageName = packageName;
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public List<FieldInfo> getFields() { return fields; }
    public void setFields(List<FieldInfo> fields) { this.fields = fields; }

    public List<String> getRelations() { return relations; }
    public void setRelations(List<String> relations) { this.relations = relations; }
}
