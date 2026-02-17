package com.ai.emailassistant.application.service;

import com.ai.emailassistant.application.repository.EmailRepository;
import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.common.ValidationConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.AIProvider;
import com.ai.emailassistant.domain.port.EmailProvider;
import com.ai.emailassistant.exception.EmailNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    private final EmailRepository emailRepository;

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

        if (index < EmailConstants.MIN_EMAIL_LIMIT) {
            throw new IllegalArgumentException(ValidationConstants.ERR_INVALID_EMAIL_INDEX);
        }

        // Step 1: Get cached email from repository
        Optional<EmailMessage> emailOpt = emailRepository.findByIndex(index);
        if (emailOpt.isEmpty()) {
            throw new EmailNotFoundException(String.format(ValidationConstants.ERR_EMAIL_NOT_FOUND_FORMAT, index));
        }
        EmailMessage email = emailOpt.get();

        // Step 2: Generate AI reply
        log.debug("Generating AI reply for email: {}", email.getEmailId());
        String replyBody = aiProvider.generateReply(email, userInstruction);

        // Step 3: Send reply
        log.debug("Sending reply to: {}", email.getFrom());
        emailProvider.reply(email.getEmailId(), replyBody);

        log.info("Successfully sent reply to email at index: {}", index);

        // Return preview (first 200 chars)
        int previewLength = EmailConstants.REPLY_PREVIEW_LENGTH;
        return replyBody.length() > previewLength 
            ? replyBody.substring(0, previewLength) + "..." 
            : replyBody;
    }

    /**
     * Execute the reply workflow using emailId:
     * 1. Retrieve email by emailId
     * 2. Generate AI reply
     * 3. Send reply via email provider
     *
     * @param emailId The unique identifier of the email
     * @param userInstruction Custom instruction for AI
     * @return Preview of the sent reply
     */
    public String executeByEmailId(String emailId, String userInstruction) {
        log.info("Replying to email: {}", emailId);

        if (emailId == null || emailId.isBlank()) {
            throw new IllegalArgumentException("Email ID cannot be null or empty");
        }

        // Step 1: Get email directly from provider
        EmailMessage email = emailProvider.getEmail(emailId);
        if (email == null) {
            throw new EmailNotFoundException("Email not found: " + emailId);
        }

        // Step 2: Generate AI reply
        log.debug("Generating AI reply for email: {}", email.getEmailId());
        String replyBody = aiProvider.generateReply(email, userInstruction);

        // Step 3: Send reply
        log.debug("Sending reply to: {}", email.getFrom());
        emailProvider.reply(email.getEmailId(), replyBody);

        log.info("Successfully sent reply to email: {}", emailId);

        // Return preview (first 200 chars)
        int previewLength = EmailConstants.REPLY_PREVIEW_LENGTH;
        return replyBody.length() > previewLength 
            ? replyBody.substring(0, previewLength) + "..." 
            : replyBody;
    }
}
