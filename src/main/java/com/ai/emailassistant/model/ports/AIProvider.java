package com.ai.emailassistant.model.ports;

import com.ai.emailassistant.model.EmailMessage;

/**
 * Port (interface) defining the contract for AI operations.
 * Implementations: local or hosted AI providers.
 */
public interface AIProvider {

    /**
     * Generate an AI-powered reply to an email.
     *
     * @param email The email message to reply to
     * @param userInstruction Custom instruction from the user
     * @return The generated reply text
     */
    String generateReply(EmailMessage email, String userInstruction);
}
