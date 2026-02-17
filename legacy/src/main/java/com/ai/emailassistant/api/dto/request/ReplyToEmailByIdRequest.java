package com.ai.emailassistant.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for replying to an email by emailId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyToEmailByIdRequest {
    
    @NotBlank(message = "Email ID is required")
    private String emailId;
    
    private String userInstruction;
}
