package com.example.backend.fileexplorer.service;

import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.fileexplorer.dto.FileContentResponse;
import com.example.backend.fileexplorer.dto.FileNodeDto;
import com.example.backend.fileexplorer.dto.SaveFileRequest;
import com.example.backend.fileexplorer.dto.SaveFileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FileExplorerService {

    private final AssessmentRepository assessmentRepository;

    public FileExplorerService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public List<FileNodeDto> getFileTree(UUID assessmentId) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        // DUMMY STUB DATA matching typical Spring Boot repository tree
        FileNodeDto noteController = new FileNodeDto("NoteController.java", "src/main/java/com/example/demo/controller/NoteController.java", "file");
        FileNodeDto noteService = new FileNodeDto("NoteService.java", "src/main/java/com/example/demo/service/NoteService.java", "file");
        FileNodeDto noteRepository = new FileNodeDto("NoteRepository.java", "src/main/java/com/example/demo/repository/NoteRepository.java", "file");
        FileNodeDto noteEntity = new FileNodeDto("Note.java", "src/main/java/com/example/demo/entity/Note.java", "file");
        FileNodeDto appMain = new FileNodeDto("DemoApplication.java", "src/main/java/com/example/demo/DemoApplication.java", "file");

        FileNodeDto controllerDir = new FileNodeDto("controller", "src/main/java/com/example/demo/controller", "directory", List.of(noteController));
        FileNodeDto serviceDir = new FileNodeDto("service", "src/main/java/com/example/demo/service", "directory", List.of(noteService));
        FileNodeDto repositoryDir = new FileNodeDto("repository", "src/main/java/com/example/demo/repository", "directory", List.of(noteRepository));
        FileNodeDto entityDir = new FileNodeDto("entity", "src/main/java/com/example/demo/entity", "directory", List.of(noteEntity));

        FileNodeDto demoPkgDir = new FileNodeDto("demo", "src/main/java/com/example/demo", "directory",
                List.of(appMain, controllerDir, serviceDir, repositoryDir, entityDir));
        FileNodeDto exampleDir = new FileNodeDto("example", "src/main/java/com/example", "directory", List.of(demoPkgDir));
        FileNodeDto comDir = new FileNodeDto("com", "src/main/java/com", "directory", List.of(exampleDir));
        FileNodeDto javaDir = new FileNodeDto("java", "src/main/java", "directory", List.of(comDir));

        FileNodeDto appProps = new FileNodeDto("application.properties", "src/main/resources/application.properties", "file");
        FileNodeDto resourcesDir = new FileNodeDto("resources", "src/main/resources", "directory", List.of(appProps));

        FileNodeDto mainDir = new FileNodeDto("main", "src/main", "directory", List.of(javaDir, resourcesDir));
        FileNodeDto testJavaDir = new FileNodeDto("java", "src/test/java", "directory");
        FileNodeDto testDir = new FileNodeDto("test", "src/test", "directory", List.of(testJavaDir));

        FileNodeDto srcDir = new FileNodeDto("src", "src", "directory", List.of(mainDir, testDir));
        FileNodeDto pomXml = new FileNodeDto("pom.xml", "pom.xml", "file");
        FileNodeDto gitIgnore = new FileNodeDto(".gitignore", ".gitignore", "file");

        return Arrays.asList(srcDir, pomXml, gitIgnore);
    }

    public FileContentResponse getFileContent(UUID assessmentId, String path) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        String filePath = (path != null && !path.trim().isEmpty()) ? path.trim() : "src/main/java/com/example/demo/controller/NoteController.java";
        String sampleContent;

        if (filePath.endsWith("NoteController.java")) {
            sampleContent = """
                    package com.example.demo.controller;

                    import com.example.demo.entity.Note;
                    import com.example.demo.service.NoteService;
                    import org.springframework.http.ResponseEntity;
                    import org.springframework.web.bind.annotation.*;

                    import java.util.List;

                    @RestController
                    @RequestMapping("/api/notes")
                    public class NoteController {

                        private final NoteService noteService;

                        public NoteController(NoteService noteService) {
                            this.noteService = noteService;
                        }

                        @GetMapping
                        public ResponseEntity<List<Note>> getAllNotes() {
                            return ResponseEntity.ok(noteService.getAllNotes());
                        }

                        @PostMapping
                        public ResponseEntity<Note> createNote(@RequestBody Note note) {
                            return ResponseEntity.ok(noteService.createNote(note));
                        }

                        // TODO: Implement search endpoint according to Feature Specification
                    }
                    """;
        } else if (filePath.endsWith("pom.xml")) {
            sampleContent = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>demo</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                    </project>
                    """;
        } else {
            sampleContent = "// Content for " + filePath + "\n";
        }

        return new FileContentResponse(filePath, sampleContent, true);
    }

    @Transactional
    public SaveFileResponse saveFile(UUID assessmentId, SaveFileRequest request) {
        // Verify assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        return new SaveFileResponse(request.getPath(), Instant.now());
    }
}
