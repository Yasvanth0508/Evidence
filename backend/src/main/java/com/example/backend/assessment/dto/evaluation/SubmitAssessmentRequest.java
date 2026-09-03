package com.example.backend.assessment.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssessmentRequest {
    private Integer tabSwitchCount;
    private Integer copyPasteEvents;
    private Integer idleTimeMinutes;
}
