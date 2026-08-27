package com.example.backend.workspace.dto;

import com.example.backend.common.enums.WorkspaceStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkspaceRequest {

    @Size(max = 200, message = "Workspace name cannot exceed 200 characters")
    private String name;

    private String description;

    private WorkspaceStatus status;
}
