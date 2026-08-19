package com.example.backend.analysis.service;

import com.example.backend.analysis.dto.*;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RepositoryAnalysisService {

    private final AssessmentRepository assessmentRepository;

    public RepositoryAnalysisService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public RepositoryAnalysisResponse getAnalysis(UUID assessmentId) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        // DUMMY STUB DATA matching the REST API specification
        ProjectStructureDto projectStructure = new ProjectStructureDto(
                Arrays.asList(
                        "src/main/java",
                        "src/main/java/com/example/demo",
                        "src/main/java/com/example/demo/controller",
                        "src/main/java/com/example/demo/service",
                        "src/main/java/com/example/demo/repository",
                        "src/main/java/com/example/demo/entity",
                        "src/main/resources",
                        "src/test/java"
                ),
                Arrays.asList(
                        "pom.xml",
                        "src/main/resources/application.properties",
                        "src/main/java/com/example/demo/DemoApplication.java",
                        "src/main/java/com/example/demo/controller/NoteController.java",
                        "src/main/java/com/example/demo/service/NoteService.java",
                        "src/main/java/com/example/demo/repository/NoteRepository.java",
                        "src/main/java/com/example/demo/entity/Note.java"
                )
        );

        SourceCodeStructureDto sourceCodeStructure = new SourceCodeStructureDto(
                List.of("NoteController"),
                List.of("NoteService"),
                List.of("NoteRepository"),
                List.of("Note")
        );

        List<Map<String, String>> endpoints = Arrays.asList(
                Map.of("path", "/api/notes", "method", "GET"),
                Map.of("path", "/api/notes", "method", "POST"),
                Map.of("path", "/api/notes/{id}", "method", "GET"),
                Map.of("path", "/api/notes/{id}", "method", "DELETE")
        );

        List<Map<String, String>> entityFields = Arrays.asList(
                Map.of("entity", "Note", "field", "id", "type", "Long"),
                Map.of("entity", "Note", "field", "title", "type", "String"),
                Map.of("entity", "Note", "field", "content", "type", "String"),
                Map.of("entity", "Note", "field", "createdAt", "type", "Instant")
        );

        List<Map<String, String>> serviceMethods = Arrays.asList(
                Map.of("service", "NoteService", "method", "getAllNotes"),
                Map.of("service", "NoteService", "method", "getNoteById"),
                Map.of("service", "NoteService", "method", "createNote"),
                Map.of("service", "NoteService", "method", "deleteNote")
        );

        ContentDetailsDto contentDetails = new ContentDetailsDto(endpoints, entityFields, serviceMethods);

        return new RepositoryAnalysisResponse(
                "COMPLETED",
                projectStructure,
                sourceCodeStructure,
                contentDetails
        );
    }

    public AnalysisStatusResponse getAnalysisStatus(UUID assessmentId) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        return new AnalysisStatusResponse("COMPLETED");
    }
}
