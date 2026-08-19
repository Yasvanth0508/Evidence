package com.example.backend.candidate.dto;

import java.util.UUID;

public class CandidateWorkspaceDto {

    private UUID workspaceId;
    private String workspaceName;

    public CandidateWorkspaceDto() {
    }

    public CandidateWorkspaceDto(UUID workspaceId, String workspaceName) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
