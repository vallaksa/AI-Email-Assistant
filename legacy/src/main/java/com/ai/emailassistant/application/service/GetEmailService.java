package com.ai.emailassistant.application.service;

import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.EmailProvider;
import com.ai.emailassistant.exception.ProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application service for getting a single email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetEmailService {
    private final EmailProvider emailProvider;

    /**
     * Get a single email by its ID.
     *
     * @param emailId The unique identifier of the email
     * @return EmailMessage object
     * @throws ProviderException if email not found or error occurs
     */
    public EmailMessage execute(String emailId) {
        log.info("Getting email: {}", emailId);

        if (emailId == null || emailId.isBlank()) {
            throw new IllegalArgumentException("Email ID cannot be null or empty");
        }

        EmailMessage email = emailProvider.getEmail(emailId);
        
        if (email == null) {
            throw new ProviderException("Email not found: " + emailId);
        }

        return email;
    }
}
