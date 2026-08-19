package com.example.backend.fileexplorer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaveFileRequest {

    @NotBlank(message = "File path is required")
    private String path;

    @NotNull(message = "File content must not be null")
    private String content;

    public SaveFileRequest() {
    }

    public SaveFileRequest(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
