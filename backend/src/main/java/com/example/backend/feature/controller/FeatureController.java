package com.example.backend.feature.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.feature.dto.FeatureSpecificationResponse;
import com.example.backend.feature.service.FeatureSpecificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/feature")
public class FeatureController {

    private final FeatureSpecificationService featureSpecificationService;

    public FeatureController(FeatureSpecificationService featureSpecificationService) {
        this.featureSpecificationService = featureSpecificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FeatureSpecificationResponse>> getFeatureSpecification(
            @PathVariable UUID assessmentId) {
        FeatureSpecificationResponse response = featureSpecificationService.getFeature(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Feature specification retrieved successfully", response));
    }
}
