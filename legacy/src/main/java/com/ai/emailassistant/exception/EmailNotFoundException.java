package com.ai.emailassistant.exception;

/**
 * Exception thrown when an email is not found.
 */
public class EmailNotFoundException extends RuntimeException {
    
    public EmailNotFoundException(String message) {
        super(message);
    }
    
    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
