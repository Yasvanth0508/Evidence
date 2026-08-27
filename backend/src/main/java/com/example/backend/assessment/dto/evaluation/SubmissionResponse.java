package com.example.backend.assessment.dto.evaluation;

import com.example.backend.common.enums.SubmissionStatus;
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
public class SubmissionResponse {

    private UUID submissionId;
    private SubmissionStatus status;
    private Instant submittedAt;
    private String message;
}
