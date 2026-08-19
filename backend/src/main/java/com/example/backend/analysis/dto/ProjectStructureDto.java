package com.example.backend.analysis.dto;

import java.util.List;

public class ProjectStructureDto {

    private List<String> folders;
    private List<String> files;

    public ProjectStructureDto() {
    }

    public ProjectStructureDto(List<String> folders, List<String> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
