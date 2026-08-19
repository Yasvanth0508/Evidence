package com.example.backend.fileexplorer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileNodeDto {

    private String name;
    private String path;
    private String type; // "file" or "directory"
    private List<FileNodeDto> children;

    public FileNodeDto() {
    }

    public FileNodeDto(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public FileNodeDto(String name, String path, String type, List<FileNodeDto> children) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<FileNodeDto> children) {
        this.children = children;
    }
}
