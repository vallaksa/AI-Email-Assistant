package com.ai.emailassistant.model.RequestResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for replying to an email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private int index;
    private String userInstruction;
}
