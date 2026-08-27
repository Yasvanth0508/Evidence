package com.example.backend.assessment.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogsResponse {

    private String logs;
    private boolean isTerminal;
}
