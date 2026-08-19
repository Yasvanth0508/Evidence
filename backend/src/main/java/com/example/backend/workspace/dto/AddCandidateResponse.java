package com.example.backend.workspace.dto;

import java.util.UUID;

public class AddCandidateResponse {

    private UUID workspaceId;
    private WorkspaceCandidateResponse candidate;

    public AddCandidateResponse() {
    }

    public AddCandidateResponse(UUID workspaceId, WorkspaceCandidateResponse candidate) {
        this.workspaceId = workspaceId;
        this.candidate = candidate;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    public WorkspaceCandidateResponse getCandidate() {
        return candidate;
    }

    public void setCandidate(WorkspaceCandidateResponse candidate) {
        this.candidate = candidate;
    }
}
