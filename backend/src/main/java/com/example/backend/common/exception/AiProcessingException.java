package com.example.backend.common.exception;

public class AiProcessingException extends RuntimeException {
    private final String errorCode;

    public AiProcessingException(String message) {
        super(message);
        this.errorCode = "AI_PROCESSING_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
