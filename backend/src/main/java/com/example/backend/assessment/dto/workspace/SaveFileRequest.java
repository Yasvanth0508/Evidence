package com.example.backend.assessment.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveFileRequest {

    @NotBlank(message = "File path must not be blank")
    private String path;

    @NotNull(message = "File content must not be null")
    private String content;
}
