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
public class RenameFileRequest {

    @NotBlank(message = "Old path must not be blank")
    private String oldPath;

    @NotBlank(message = "New path must not be blank")
    private String newPath;
}
