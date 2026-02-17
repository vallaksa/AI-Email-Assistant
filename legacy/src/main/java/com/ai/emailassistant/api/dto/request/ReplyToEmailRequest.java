package com.ai.emailassistant.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for replying to an email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyToEmailRequest {
    
    @NotNull(message = "Index is required")
    @Min(value = 1, message = "Index must be greater than 0")
    private Integer index;
    
    private String userInstruction;
}
