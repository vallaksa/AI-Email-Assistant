package com.ai.emailassistant.infrastructure.ai;

import com.ai.emailassistant.model.EmailMessage;

/**
 * Shared prompt builder for AI providers.
 */
final class PromptBuilder {
    private PromptBuilder() {
    }

    static String buildPrompt(EmailMessage email, String userInstruction) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional email assistant. Generate a concise, ")
                .append("polite, and professional email reply.\n\n");

        prompt.append("=== ORIGINAL EMAIL ===\n");
        prompt.append("From: ").append(email.getFrom()).append("\n");
        prompt.append("Subject: ").append(email.getSubject()).append("\n\n");
        prompt.append("Body:\n").append(email.getSnippet()).append("\n\n");

        if (userInstruction != null && !userInstruction.isBlank()) {
            prompt.append("=== INSTRUCTIONS ===\n");
            prompt.append(userInstruction).append("\n\n");
        }

        prompt.append("=== REPLY ===\n");
        prompt.append("Generate ONLY the email body (no subject, no greeting, no closing):\n");

        return prompt.toString();
    }
}
