package com.example.backend.fileexplorer.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.fileexplorer.dto.FileContentResponse;
import com.example.backend.fileexplorer.dto.FileNodeDto;
import com.example.backend.fileexplorer.dto.SaveFileRequest;
import com.example.backend.fileexplorer.dto.SaveFileResponse;
import com.example.backend.fileexplorer.service.FileExplorerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/files")
public class FileExplorerController {

    private final FileExplorerService fileExplorerService;

    public FileExplorerController(FileExplorerService fileExplorerService) {
        this.fileExplorerService = fileExplorerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileNodeDto>>> getFileTree(@PathVariable UUID assessmentId) {
        List<FileNodeDto> response = fileExplorerService.getFileTree(assessmentId);
        return ResponseEntity.ok(ApiResponse.success("File tree retrieved successfully", response));
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<FileContentResponse>> getFileContent(
            @PathVariable UUID assessmentId,
            @RequestParam(required = false) String path) {
        FileContentResponse response = fileExplorerService.getFileContent(assessmentId, path);
        return ResponseEntity.ok(ApiResponse.success("File content retrieved successfully", response));
    }

    @PutMapping("/content")
    public ResponseEntity<ApiResponse<SaveFileResponse>> saveFileContent(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody SaveFileRequest request) {
        SaveFileResponse response = fileExplorerService.saveFile(assessmentId, request);
        return ResponseEntity.ok(ApiResponse.success("File saved successfully", response));
    }
}
