package com.example.backend.workspace.dto;

import com.example.backend.common.enums.WorkspaceStatus;

import java.util.UUID;

public class WorkspaceResponse {

    private UUID id;
    private String name;
    private String description;
    private WorkspaceStatus status;

    public WorkspaceResponse() {
    }

    public WorkspaceResponse(UUID id, String name, String description, WorkspaceStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }
}
