package com.ai.emailassistant.controller;

import com.ai.emailassistant.model.RequestResponse.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EmailResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(EmailResponse.error("Invalid request", ex.getMessage()));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<EmailResponse<Void>> handleBadRequest(Exception ex) {
        log.warn("Malformed request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(EmailResponse.error("Invalid request", "Malformed or invalid request data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmailResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(EmailResponse.error("Internal server error", "Failed to process request"));
    }
}
