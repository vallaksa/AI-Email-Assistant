package com.ai.emailassistant.model.ports;

import com.ai.emailassistant.model.EmailMessage;
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
     * Send a reply to an email.
     *
     * @param emailId The unique identifier of the email to reply to
     * @param replyBody The body of the reply message
     */
    void reply(String emailId, String replyBody);
}
