package com.example.backend.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedCandidateResponse {

    private UUID id;
    private UUID workspaceId;
    private String workspaceName;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private UUID assessmentId;
    private BigDecimal score;
    private String scoreRating;
    private Integer passedTests;
    private Integer totalTests;
    private Long timeTakenMinutes;
    private String candidateRole;
    private String selectionNotes;
    private String selectionStatus;
    private Instant selectedAt;
}
