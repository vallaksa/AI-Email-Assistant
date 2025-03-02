package com.AI_Powered.Email.Assistant.model;

public class ReplyRequest {

    private String message;

    // Default constructor for Jackson
    public ReplyRequest() {
    }

    public ReplyRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 