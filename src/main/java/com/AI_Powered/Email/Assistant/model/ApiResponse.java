package com.AI_Powered.Email.Assistant.model;

public class ApiResponse {

    private String status;
    private String message;
    private String error;

    // Default constructor for Jackson
    public ApiResponse() {
    }

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiResponse(String status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse("success", message);
    }

    public static ApiResponse error(String errorCode, String message) {
        return new ApiResponse("error", message, errorCode);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
} 