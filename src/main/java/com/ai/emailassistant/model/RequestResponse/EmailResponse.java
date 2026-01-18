package com.ai.emailassistant.model.RequestResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API response model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> EmailResponse<T> ok(String message, T data) {
        return EmailResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> EmailResponse<T> error(String message, String error) {
        return EmailResponse.<T>builder()
            .success(false)
            .message(message)
            .error(error)
            .build();
    }
}
