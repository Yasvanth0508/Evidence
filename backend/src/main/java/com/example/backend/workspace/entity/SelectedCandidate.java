package com.example.backend.workspace.entity;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.auth.entity.User;
import com.example.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "selected_candidates")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedCandidate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @Column(name = "selection_notes", columnDefinition = "TEXT")
    private String selectionNotes;

    @Column(name = "selection_status", length = 50, nullable = false)
    @Builder.Default
    private String selectionStatus = "SELECTED";

    @Column(name = "selected_at", nullable = false)
    @Builder.Default
    private Instant selectedAt = Instant.now();

    @PrePersist
    protected void onPrePersist() {
        if (this.selectedAt == null) {
            this.selectedAt = Instant.now();
        }
    }
}
