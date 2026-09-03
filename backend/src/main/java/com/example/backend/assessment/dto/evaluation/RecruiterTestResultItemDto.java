package com.example.backend.assessment.dto.evaluation;

import com.example.backend.common.enums.TestResultStatus;
import com.example.backend.common.enums.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterTestResultItemDto {

    private UUID testCaseId;
    private Integer testCaseNumber;
    private TestType testType;
    private TestResultStatus status;
    private String httpMethod;
    private String endpoint;
    private Integer expectedStatusCode;
    private Integer actualStatusCode;
    private String expectedResponse;
    private String actualResponse;
    private String assertions;
    private Long executionTimeMs;
    private BigDecimal weight;
    private String failureReason;
}
