package com.example.backend.feature.service;

import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.feature.dto.FeatureSpecificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeatureSpecificationService {

    private final AssessmentRepository assessmentRepository;

    public FeatureSpecificationService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public FeatureSpecificationResponse getFeature(UUID assessmentId) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        // DUMMY STUB DATA matching REST API specification
        List<String> requirements = Arrays.asList(
                "Create a GET /api/notes/search endpoint in NoteController",
                "Accept 'keyword' as a required query parameter (?keyword={value})",
                "Filter notes where title or content contains keyword (case-insensitive search)",
                "Return HTTP 200 with an array of matching Note objects",
                "Return HTTP 400 with errorCode VALIDATION_ERROR if keyword parameter is missing or empty"
        );

        Map<String, Object> requestSpec = Map.of(
                "queryParameters", Map.of(
                        "keyword", Map.of(
                                "type", "string",
                                "required", true,
                                "description", "Keyword to search within title or content"
                        )
                )
        );

        Map<String, Object> responseSpec = Map.of(
                "status", 200,
                "body", Map.of(
                        "type", "array",
                        "items", "Note object",
                        "example", List.of(
                                Map.of(
                                        "id", 1,
                                        "title", "Meeting Notes",
                                        "content", "Discuss Spring Boot architecture",
                                        "createdAt", "2026-08-19T10:00:00Z"
                                )
                        )
                )
        );

        List<String> constraints = Arrays.asList(
                "Use Spring Data JPA repository query methods or @Query annotation",
                "Do not modify existing /api/notes CRUD endpoints",
                "Ensure case-insensitive matching using LOWER() or containingIgnoreCase",
                "Ensure proper error handling with custom exceptions if needed"
        );

        return new FeatureSpecificationResponse(
                "Implement Note Search Endpoint with Keyword Filtering",
                "Add a practical search feature to the existing note-taking application allowing users to search notes by keyword.",
                requirements,
                "/api/notes/search",
                "GET",
                requestSpec,
                responseSpec,
                constraints
        );
    }
}
