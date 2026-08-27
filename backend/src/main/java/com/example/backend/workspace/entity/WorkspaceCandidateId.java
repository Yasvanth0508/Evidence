package com.example.backend.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCandidateId implements Serializable {

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;
}
