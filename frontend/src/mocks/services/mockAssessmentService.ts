import {
  ApiResponse,
  Assessment,
  FeatureSpecification,
  FileNode,
  ProcessingStatusResponse,
  Workspace,
} from "@/types";
import {
  mockAssessmentsList,
  mockFeatureSpec,
  mockFileTree,
  mockProcessingStatus,
  mockWorkspaces,
} from "../data/assessment.mock";

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const mockAssessmentService = {
  getWorkspaces: async (): Promise<ApiResponse<Workspace[]>> => {
    await delay(300);
    return {
      success: true,
      message: "Workspaces retrieved successfully",
      data: mockWorkspaces,
      timestamp: new Date().toISOString(),
    };
  },

  getAssessments: async (): Promise<ApiResponse<Assessment[]>> => {
    await delay(400);
    return {
      success: true,
      message: "Assessments retrieved successfully",
      data: mockAssessmentsList,
      timestamp: new Date().toISOString(),
    };
  },

  getAssessmentById: async (id: string): Promise<ApiResponse<Assessment>> => {
    await delay(350);
    const assessment =
      mockAssessmentsList.find((a) => a.id === id) || mockAssessmentsList[0];
    return {
      success: true,
      message: "Assessment found",
      data: assessment,
      timestamp: new Date().toISOString(),
    };
  },

  getProcessingStatus: async (
    _id: string
  ): Promise<ApiResponse<ProcessingStatusResponse>> => {
    await delay(300);
    return {
      success: true,
      message: "Processing status retrieved",
      data: mockProcessingStatus,
      timestamp: new Date().toISOString(),
    };
  },

  getFeatureSpecification: async (
    _id: string
  ): Promise<ApiResponse<FeatureSpecification>> => {
    await delay(300);
    return {
      success: true,
      message: "Feature specification retrieved",
      data: mockFeatureSpec,
      timestamp: new Date().toISOString(),
    };
  },

  getFileTree: async (_id: string): Promise<ApiResponse<FileNode[]>> => {
    await delay(400);
    return {
      success: true,
      message: "Workspace file tree retrieved",
      data: mockFileTree,
      timestamp: new Date().toISOString(),
    };
  },

  getFileContent: async (
    _id: string,
    path: string
  ): Promise<ApiResponse<{ path: string; content: string }>> => {
    await delay(250);
    let sampleContent = "// Java source code file\npackage com.example.notes;\n\npublic class Demo {\n  // Code goes here\n}\n";
    if (path.endsWith("NoteController.java")) {
      sampleContent = `package com.example.notes;

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
        return ResponseEntity.ok(noteService.findAll());
    }

    // TODO: Implement GET /api/notes/search
}
`;
    } else if (path.endsWith("pom.xml")) {
      sampleContent = `<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>notes-service</artifactId>
  <version>1.0.0</version>
</project>`;
    }
    return {
      success: true,
      message: "File content retrieved",
      data: { path, content: sampleContent },
      timestamp: new Date().toISOString(),
    };
  },

  saveFile: async (
    _id: string,
    path: string,
    _content: string
  ): Promise<ApiResponse<{ path: string; savedAt: string }>> => {
    await delay(300);
    return {
      success: true,
      message: "File saved successfully",
      data: { path, savedAt: new Date().toISOString() },
      timestamp: new Date().toISOString(),
    };
  },
};
