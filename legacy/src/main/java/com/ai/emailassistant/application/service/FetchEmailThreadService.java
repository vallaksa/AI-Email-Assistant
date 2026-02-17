package com.ai.emailassistant.application.service;

import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.EmailProvider;
import com.ai.emailassistant.exception.ProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for fetching email threads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FetchEmailThreadService {
    private final EmailProvider emailProvider;

    /**
     * Fetch all messages in a thread.
     *
     * @param threadId The unique identifier of the thread
     * @return List of EmailMessage objects in the thread
     * @throws ProviderException if thread not found or error occurs
     */
    public List<EmailMessage> execute(String threadId) {
        log.info("Fetching thread: {}", threadId);

        if (threadId == null || threadId.isBlank()) {
            throw new IllegalArgumentException("Thread ID cannot be null or empty");
        }

        List<EmailMessage> messages = emailProvider.getThread(threadId);
        
        if (messages == null || messages.isEmpty()) {
            throw new ProviderException("Thread not found or empty: " + threadId);
        }

        return messages;
    }
}
