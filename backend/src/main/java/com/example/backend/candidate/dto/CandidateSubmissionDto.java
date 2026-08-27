package com.example.backend.candidate.dto;

import com.example.backend.common.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSubmissionDto {

    private Instant submittedAt;
    private Long timeTakenSeconds;
    private SubmissionStatus status;
}
