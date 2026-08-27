package com.example.backend.workspace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCandidateRequest {

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String name;
}
