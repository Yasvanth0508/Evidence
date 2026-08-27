package com.example.backend.assessment.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileNodeDto {

    private String name;
    private String type; // "FILE" or "DIRECTORY"
    private String path;
    private String extension;
    private Long sizeBytes;

    @Builder.Default
    private List<FileNodeDto> children = new ArrayList<>();
}
