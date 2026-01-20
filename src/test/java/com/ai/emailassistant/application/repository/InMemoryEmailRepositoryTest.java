package com.ai.emailassistant.application.repository;

import com.ai.emailassistant.domain.model.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEmailRepositoryTest {

    private InMemoryEmailRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEmailRepository();
    }

    @Test
    void saveAll_savesEmailsToRepository() {
        EmailMessage email1 = EmailMessage.builder()
            .index(1)
            .emailId("id-1")
            .from("sender1@example.com")
            .subject("Subject 1")
            .snippet("Snippet 1")
            .receivedAt(Instant.now())
            .build();
        
        EmailMessage email2 = EmailMessage.builder()
            .index(2)
            .emailId("id-2")
            .from("sender2@example.com")
            .subject("Subject 2")
            .snippet("Snippet 2")
            .receivedAt(Instant.now())
            .build();

        repository.saveAll(List.of(email1, email2));

        Optional<EmailMessage> found = repository.findByIndex(1);
        assertThat(found).isPresent();
        assertThat(found.get().getEmailId()).isEqualTo("id-1");
    }

    @Test
    void findByIndex_returnsEmptyWhenNotFound() {
        Optional<EmailMessage> found = repository.findByIndex(999);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedEmails() {
        EmailMessage email1 = EmailMessage.builder()
            .index(1)
            .emailId("id-1")
            .from("sender1@example.com")
            .subject("Subject 1")
            .snippet("Snippet 1")
            .receivedAt(Instant.now())
            .build();
        
        EmailMessage email2 = EmailMessage.builder()
            .index(2)
            .emailId("id-2")
            .from("sender2@example.com")
            .subject("Subject 2")
            .snippet("Snippet 2")
            .receivedAt(Instant.now())
            .build();

        repository.saveAll(List.of(email1, email2));

        List<EmailMessage> all = repository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void clear_removesAllEmails() {
        EmailMessage email = EmailMessage.builder()
            .index(1)
            .emailId("id-1")
            .from("sender@example.com")
            .subject("Subject")
            .snippet("Snippet")
            .receivedAt(Instant.now())
            .build();

        repository.saveAll(List.of(email));
        repository.clear();

        Optional<EmailMessage> found = repository.findByIndex(1);
        assertThat(found).isEmpty();
    }

    @Test
    void saveAll_clearsPreviousEntries() {
        EmailMessage email1 = EmailMessage.builder()
            .index(1)
            .emailId("id-1")
            .from("sender1@example.com")
            .subject("Subject 1")
            .snippet("Snippet 1")
            .receivedAt(Instant.now())
            .build();

        repository.saveAll(List.of(email1));

        EmailMessage email2 = EmailMessage.builder()
            .index(1)
            .emailId("id-2")
            .from("sender2@example.com")
            .subject("Subject 2")
            .snippet("Snippet 2")
            .receivedAt(Instant.now())
            .build();

        repository.saveAll(List.of(email2));

        Optional<EmailMessage> found = repository.findByIndex(1);
        assertThat(found).isPresent();
        assertThat(found.get().getEmailId()).isEqualTo("id-2");
    }
}
