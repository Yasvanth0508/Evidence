package com.example.backend.fileexplorer.dto;

public class FileContentResponse {

    private String path;
    private String content;
    private Boolean isEditable;

    public FileContentResponse() {
    }

    public FileContentResponse(String path, String content, Boolean isEditable) {
        this.path = path;
        this.content = content;
        this.isEditable = isEditable;
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

    public Boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(Boolean editable) {
        isEditable = editable;
    }
}
