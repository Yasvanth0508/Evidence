package com.example.backend.assessment.dto.workspace;

import com.example.backend.common.enums.AssessmentStatus;
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
public class StartAssessmentResponse {

    private UUID assessmentId;
    private AssessmentStatus status;
    private String workspacePath;
    private Instant startedAt;
}
