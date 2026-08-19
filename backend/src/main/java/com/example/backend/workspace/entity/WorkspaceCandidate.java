package com.example.backend.workspace.entity;

import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(
    name = "workspace_candidates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspace_candidate", columnNames = {"workspace_id", "candidate_id"})
    }
)
public class WorkspaceCandidate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    public WorkspaceCandidate() {
    }

    public WorkspaceCandidate(Workspace workspace, User candidate) {
        this.workspace = workspace;
        this.candidate = candidate;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }
}
