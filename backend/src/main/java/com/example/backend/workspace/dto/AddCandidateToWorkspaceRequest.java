package com.example.backend.workspace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AddCandidateToWorkspaceRequest {

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Candidate email must be a valid email address")
    private String email;

    public AddCandidateToWorkspaceRequest() {
    }

    public AddCandidateToWorkspaceRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
