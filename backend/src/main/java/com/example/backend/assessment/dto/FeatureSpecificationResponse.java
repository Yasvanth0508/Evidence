package com.example.backend.assessment.dto;

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
public class FeatureSpecificationResponse {

    private UUID assessmentId;
    private String title;
    private String featureName;
    private String description;
    private String endpoint;
    private String httpMethod;
    private Object requirements;
    private Object requestSpecification;
    private Object responseSpecification;
    private Object constraints;
    private Instant createdAt;
    private Instant updatedAt;
}
