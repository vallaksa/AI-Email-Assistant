package com.AI_Powered.Email.Assistant.model;

public class EmailResponse {

    private String from;
    private String subject;
    private String date;

    // Default constructor for Jackson
    public EmailResponse() {
    }

    public EmailResponse(String from, String subject, String date) {
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
} 