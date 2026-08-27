package com.example.backend.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCandidateItemResponse {

    private UUID workspaceId;
    private CandidateDto candidate;
    private Instant createdAt;
}
