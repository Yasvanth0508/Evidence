package com.example.backend.workspace.entity;

import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.WorkspaceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspaces")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;
}
