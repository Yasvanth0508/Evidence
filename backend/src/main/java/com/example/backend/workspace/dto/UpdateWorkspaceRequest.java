package com.example.backend.workspace.dto;

import jakarta.validation.constraints.Size;

public class UpdateWorkspaceRequest {

    @Size(max = 200, message = "Workspace name must not exceed 200 characters")
    private String name;

    private String description;

    public UpdateWorkspaceRequest() {
    }

    public UpdateWorkspaceRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
