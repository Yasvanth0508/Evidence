package com.example.backend.workspace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedCandidateRequest {

    @NotNull(message = "workspaceId is required")
    private UUID workspaceId;

    @NotNull(message = "candidateId is required")
    private UUID candidateId;

    private UUID assessmentId;

    private String selectionNotes;

    private String selectionStatus;
}
