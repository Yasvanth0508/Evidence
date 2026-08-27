package com.example.backend.workspace.entity;

import com.example.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "workspace_candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceCandidate {

    @EmbeddedId
    private WorkspaceCandidateId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WorkspaceCandidate(Workspace workspace, User candidate) {
        this.workspace = workspace;
        this.candidate = candidate;
        this.id = new WorkspaceCandidateId(
                workspace != null ? workspace.getId() : null,
                candidate != null ? candidate.getId() : null
        );
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.id == null && workspace != null && candidate != null) {
            this.id = new WorkspaceCandidateId(workspace.getId(), candidate.getId());
        }
    }
}
