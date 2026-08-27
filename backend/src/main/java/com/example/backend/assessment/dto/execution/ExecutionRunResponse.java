package com.example.backend.assessment.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRunResponse {

    private UUID executionId;
    private String status; // "STARTING", "RUNNING", "FAILED"
    private Integer port;
    private String message;
}
