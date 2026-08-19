package com.example.backend.fileexplorer.dto;

import java.time.Instant;

public class SaveFileResponse {

    private String path;
    private Instant savedAt;

    public SaveFileResponse() {
    }

    public SaveFileResponse(String path, Instant savedAt) {
        this.path = path;
        this.savedAt = savedAt;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Instant savedAt) {
        this.savedAt = savedAt;
    }
}
