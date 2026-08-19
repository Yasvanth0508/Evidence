package com.example.backend.common.exception;

public class ExecutionUnavailableException extends RuntimeException {
    private final String errorCode;

    public ExecutionUnavailableException(String message) {
        super(message);
        this.errorCode = "EXECUTION_UNAVAILABLE";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
