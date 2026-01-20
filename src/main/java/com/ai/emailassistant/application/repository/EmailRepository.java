package com.ai.emailassistant.application.repository;

import com.ai.emailassistant.domain.model.EmailMessage;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for email caching operations.
 */
public interface EmailRepository {

    /**
     * Save all emails to the repository, clearing previous entries.
     *
     * @param emails List of emails to save
     */
    void saveAll(List<EmailMessage> emails);

    /**
     * Find an email by its index (1-based).
     *
     * @param index 1-based index
     * @return Optional containing the email if found
     */
    Optional<EmailMessage> findByIndex(int index);

    /**
     * Get all cached emails.
     *
     * @return List of all cached emails
     */
    List<EmailMessage> findAll();

    /**
     * Clear all cached emails.
     */
    void clear();
}
