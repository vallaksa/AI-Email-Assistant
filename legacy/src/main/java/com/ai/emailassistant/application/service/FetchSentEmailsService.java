package com.ai.emailassistant.application.service;

import com.ai.emailassistant.application.repository.EmailRepository;
import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.common.ValidationConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for fetching sent emails.
 * Orchestrates the use case: "Fetch recent sent emails from Gmail"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FetchSentEmailsService {
    private final EmailProvider emailProvider;
    private final EmailRepository emailRepository;

    /**
     * Fetch the most recent sent emails and cache them.
     *
     * @param limit Number of emails to fetch
     * @return List of EmailMessage objects
     */
    public List<EmailMessage> execute(int limit) {
        log.info("Fetching {} sent emails", limit);

        // Validate input
        if (limit < EmailConstants.MIN_EMAIL_LIMIT || limit > EmailConstants.MAX_EMAIL_LIMIT) {
            throw new IllegalArgumentException(ValidationConstants.ERR_INVALID_EMAIL_LIMIT);
        }

        // Fetch from provider
        List<EmailMessage> emails = emailProvider.fetchSent(limit);

        // Set indices and cache the results
        for (int i = 0; i < emails.size(); i++) {
            emails.get(i).setIndex(i + 1);
        }

        // Save to repository
        emailRepository.saveAll(emails);

        log.info("Fetched and cached {} sent emails", emails.size());
        return emails;
    }
}
