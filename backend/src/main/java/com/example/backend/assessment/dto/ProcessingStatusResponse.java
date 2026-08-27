package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStatusResponse {

    private UUID assessmentId;
    private AssessmentStatus assessmentStatus;
    private RepositoryAnalysisProcessingDto repositoryAnalysis;
    private FeatureSpecificationProcessingDto featureSpecification;
    private TestCaseProcessingDto testCases;
}
