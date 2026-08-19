package com.example.backend.workspace.entity;

import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.WorkspaceStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "workspaces")
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
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    public Workspace() {
    }

    public Workspace(User recruiter, String name, String description, WorkspaceStatus status) {
        this.recruiter = recruiter;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : WorkspaceStatus.ACTIVE;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
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
