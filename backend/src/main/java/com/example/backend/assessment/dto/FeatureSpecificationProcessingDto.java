package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AnalysisStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureSpecificationProcessingDto {

    private AnalysisStatus status;
    private boolean available;
}
