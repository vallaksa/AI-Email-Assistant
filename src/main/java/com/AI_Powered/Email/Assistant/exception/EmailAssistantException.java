package com.AI_Powered.Email.Assistant.exception;

/**
 * Custom exception class for Email Assistant application.
 * Used to wrap and provide additional context for exceptions that occur during email operations.
 */
public class EmailAssistantException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public EmailAssistantException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }
    
    public EmailAssistantException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }
    
    public EmailAssistantException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public EmailAssistantException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Error codes for different types of exceptions in the application.
     */
    public enum ErrorCode {
        GENERAL_ERROR,
        EMAIL_FETCH_ERROR,
        EMAIL_SEND_ERROR,
        EMAIL_REPLY_ERROR,
        AI_SERVICE_ERROR,
        AUTHENTICATION_ERROR,
        INVALID_REQUEST,
        CONFIGURATION_ERROR
    }
} 