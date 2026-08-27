package com.example.backend.assessment.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFileRequest {

    @NotBlank(message = "Path must not be blank")
    private String path;

    @Builder.Default
    private String type = "FILE"; // "FILE" or "DIRECTORY"

    private String initialContent;
}
