package com.example.backend.common.exception;

public class AssessmentAlreadySubmittedException extends RuntimeException {
    private final String errorCode;

    public AssessmentAlreadySubmittedException(String message) {
        super(message);
        this.errorCode = "ASSESSMENT_ALREADY_SUBMITTED";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
