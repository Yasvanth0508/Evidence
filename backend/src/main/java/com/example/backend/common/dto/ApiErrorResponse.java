package com.example.backend.common.dto;

import java.time.Instant;

public class ApiErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private String timestamp;

    public ApiErrorResponse() {
        this.success = false;
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now().toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
