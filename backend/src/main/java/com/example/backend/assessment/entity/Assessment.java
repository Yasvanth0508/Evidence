package com.example.backend.assessment.entity;

import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
import com.example.backend.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "assessments")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "repository_url", nullable = false, columnDefinition = "TEXT")
    private String repositoryUrl;

    @Column(name = "branch_name", nullable = false, length = 255)
    private String branchName;

    @Column(name = "backend_root_directory", nullable = false, columnDefinition = "TEXT")
    private String backendRootDirectory;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 50)
    private Difficulty difficulty;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "scheduled_start_at", nullable = false)
    private Instant scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private Instant scheduledEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private AssessmentStatus status = AssessmentStatus.CREATING;
}
