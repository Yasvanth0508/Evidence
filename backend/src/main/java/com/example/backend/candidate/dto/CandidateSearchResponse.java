package com.example.backend.candidate.dto;

import com.example.backend.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSearchResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;
}
