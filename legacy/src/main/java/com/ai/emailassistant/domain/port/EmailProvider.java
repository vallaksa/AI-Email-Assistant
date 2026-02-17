package com.ai.emailassistant.domain.port;

import com.ai.emailassistant.domain.model.EmailMessage;
import java.util.List;

/**
 * Port (interface) defining the contract for email operations.
 * Implementations: GmailEmailProvider, etc.
 */
public interface EmailProvider {

    /**
     * Fetch the most recent emails.
     *
     * @param limit Number of emails to fetch (1-50)
     * @return List of EmailMessage objects
     */
    List<EmailMessage> fetchLatest(int limit);

    /**
     * Fetch the most recent sent emails.
     *
     * @param limit Number of emails to fetch (1-50)
     * @return List of EmailMessage objects
     */
    List<EmailMessage> fetchSent(int limit);

    /**
     * Get a single email by its ID.
     *
     * @param emailId The unique identifier of the email
     * @return EmailMessage object, or null if not found
     */
    EmailMessage getEmail(String emailId);

    /**
     * Get all messages in a thread.
     *
     * @param threadId The unique identifier of the thread
     * @return List of EmailMessage objects in the thread
     */
    List<EmailMessage> getThread(String threadId);

    /**
     * Send a reply to an email.
     *
     * @param emailId The unique identifier of the email to reply to
     * @param replyBody The body of the reply message
     */
    void reply(String emailId, String replyBody);
}
