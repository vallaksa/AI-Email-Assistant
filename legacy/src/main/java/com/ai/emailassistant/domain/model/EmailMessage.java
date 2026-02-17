package com.ai.emailassistant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing an email message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage {
    private int index;
    private String emailId;
    private String from;
    private String subject;
    private String snippet;
    private String body;
    private String bodyType; // "html" or "text"
    private String threadId;
    private Instant receivedAt;
}
