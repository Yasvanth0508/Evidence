package com.example.backend.assessment.dto.execution;

import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStatusResponse {

    private UUID executionId;
    private BuildStatus buildStatus;
    private ContainerStatus containerStatus;
    private ApplicationStatus applicationStatus;
    private Integer port;
    private Long uptimeSeconds;
    private String errorMessage;
}
