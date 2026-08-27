package com.example.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Builder.Default
    private boolean success = false;
    private String message;
    private String errorCode;
    private Map<String, String> errors;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public ApiErrorResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(String message, String errorCode, Map<String, String> errors) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.errors = errors;
        this.timestamp = Instant.now();
    }
}
