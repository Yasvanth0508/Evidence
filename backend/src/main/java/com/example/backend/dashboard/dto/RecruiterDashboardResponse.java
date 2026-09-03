package com.example.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterDashboardResponse {

    private long workspaceCount;
    private long candidateCount;
    private long assessmentCount;
    private long activeAssessments;
    private long completedAssessments;
    private long selectedCandidates;
}
