package com.example.backend.assessment.dto;

import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
import com.example.backend.workspace.dto.CandidateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDetailResponse {

    private UUID id;
    private UUID workspaceId;
    private CandidateDto candidate;
    private String repositoryUrl;
    private String branchName;
    private String backendRootDirectory;
    private Difficulty difficulty;
    private Integer durationMinutes;
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private AssessmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
