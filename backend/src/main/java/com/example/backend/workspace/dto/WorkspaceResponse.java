package com.example.backend.workspace.dto;

import com.example.backend.common.enums.WorkspaceStatus;
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
public class WorkspaceResponse {

    private UUID id;
    private UUID recruiterId;
    private String name;
    private String description;
    private WorkspaceStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
