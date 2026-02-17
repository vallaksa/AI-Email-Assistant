package com.ai.emailassistant.exception;

/**
 * Exception thrown when a provider (email or AI) operation fails.
 */
public class ProviderException extends RuntimeException {
    
    public ProviderException(String message) {
        super(message);
    }
    
    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
