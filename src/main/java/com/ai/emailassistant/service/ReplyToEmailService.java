package com.ai.emailassistant.service;

import com.ai.emailassistant.model.model.EmailMessage;
import com.ai.emailassistant.model.ports.AIProvider;
import com.ai.emailassistant.model.ports.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application service for replying to emails.
 * Orchestrates the use case: "Generate and send an AI reply to a selected email"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyToEmailService {
    private final EmailProvider emailProvider;
    private final AIProvider aiProvider;
    private final FetchEmailsService fetchEmailsService;

    /**
     * Execute the reply workflow:
     * 1. Retrieve cached email by index
     * 2. Generate AI reply
     * 3. Send reply via email provider
     *
     * @param index 1-based email index from last fetch
     * @param userInstruction Custom instruction for AI
     * @return Preview of the sent reply
     */
    public String execute(int index, String userInstruction) {
        log.info("Replying to email at index: {}", index);

        // Step 1: Get cached email
        EmailMessage email = fetchEmailsService.getCachedEmail(index);
        if (email == null) {
            throw new IllegalArgumentException("Email not found at index: " + index);
        }

        // Step 2: Generate AI reply
        log.debug("Generating AI reply for email: {}", email.getEmailId());
        String replyBody = aiProvider.generateReply(email, userInstruction);

        // Step 3: Send reply
        log.debug("Sending reply to: {}", email.getFrom());
        emailProvider.reply(email.getEmailId(), replyBody);

        log.info("Successfully sent reply to email at index: {}", index);

        // Return preview (first 200 chars)
        return replyBody.length() > 200 ? replyBody.substring(0, 200) + "..." : replyBody;
    }
}
