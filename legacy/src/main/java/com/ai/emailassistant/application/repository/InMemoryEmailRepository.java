package com.ai.emailassistant.application.repository;

import com.ai.emailassistant.domain.model.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe in-memory implementation of EmailRepository.
 * Uses ConcurrentHashMap for thread safety.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InMemoryEmailRepository implements EmailRepository {

    private final ConcurrentMap<Integer, EmailMessage> emailCache = new ConcurrentHashMap<>();

    @Override
    public void saveAll(List<EmailMessage> emails) {
        log.debug("Saving {} emails to repository", emails.size());
        emailCache.clear();
        for (EmailMessage email : emails) {
            emailCache.put(email.getIndex(), email);
        }
        log.debug("Repository now contains {} emails", emailCache.size());
    }

    @Override
    public Optional<EmailMessage> findByIndex(int index) {
        EmailMessage email = emailCache.get(index);
        if (email != null) {
            log.debug("Found email at index {}", index);
            return Optional.of(email);
        }
        log.debug("No email found at index {}", index);
        return Optional.empty();
    }

    @Override
    public List<EmailMessage> findAll() {
        return new ArrayList<>(emailCache.values());
    }

    @Override
    public void clear() {
        log.debug("Clearing email repository");
        emailCache.clear();
    }
}
