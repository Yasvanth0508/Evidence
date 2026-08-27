package com.example.backend.assessment.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContentResponse {

    private String path;
    private String content;
    private String language;
    private Long sizeBytes;
}
