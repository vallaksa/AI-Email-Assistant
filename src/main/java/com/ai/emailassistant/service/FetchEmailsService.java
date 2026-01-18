package com.ai.emailassistant.service;

import com.ai.emailassistant.model.EmailMessage;
import com.ai.emailassistant.model.ports.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service for fetching emails.
 * Orchestrates the use case: "Fetch recent emails from Gmail"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FetchEmailsService {
    private final EmailProvider emailProvider;

    // In-memory cache of last fetched emails (V1 constraint)
    private Map<Integer, EmailMessage> lastFetchedEmails = new HashMap<>();

    /**
     * Fetch the most recent emails and cache them.
     *
     * @param limit Number of emails to fetch
     * @return List of EmailMessage objects
     */
    public List<EmailMessage> execute(int limit) {
        log.info("Fetching {} emails", limit);

        // Validate input
        if (limit <= 0 || limit > 50) {
            throw new IllegalArgumentException("Limit must be between 1 and 50");
        }

        // Fetch from provider
        List<EmailMessage> emails = emailProvider.fetchLatest(limit);

        // Cache the results with index mapping
        lastFetchedEmails.clear();
        for (int i = 0; i < emails.size(); i++) {
            emails.get(i).setIndex(i + 1);
            lastFetchedEmails.put(i + 1, emails.get(i));
        }

        log.info("Fetched and cached {} emails", emails.size());
        return emails;
    }

    /**
     * Get the cached email by index.
     *
     * @param index 1-based index
     * @return EmailMessage or null if not found
     */
    public EmailMessage getCachedEmail(int index) {
        return lastFetchedEmails.get(index);
    }
}
