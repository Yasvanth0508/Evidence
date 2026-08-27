package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AnalysisStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryAnalysisProcessingDto {

    private AnalysisStatus status;
    private Instant completedAt;
}
