package com.example.backend.assessment.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoreDto {
    private String category;
    private Integer total;
    private Integer passed;
    private Integer score;
}
