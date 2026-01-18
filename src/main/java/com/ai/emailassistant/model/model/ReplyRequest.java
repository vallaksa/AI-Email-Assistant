package com.ai.emailassistant.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for replying to an email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyRequest {
    private int index;
    private String userInstruction;
}
