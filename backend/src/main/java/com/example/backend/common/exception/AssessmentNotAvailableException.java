package com.example.backend.common.exception;

public class AssessmentNotAvailableException extends RuntimeException {
    private final String errorCode;

    public AssessmentNotAvailableException(String message) {
        super(message);
        this.errorCode = "ASSESSMENT_NOT_AVAILABLE";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
