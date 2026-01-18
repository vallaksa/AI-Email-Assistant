package com.ai.emailassistant.service;

import com.ai.emailassistant.model.EmailMessage;
import com.ai.emailassistant.model.ports.EmailProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchEmailsServiceTest {

    @Mock
    private EmailProvider emailProvider;

    @InjectMocks
    private FetchEmailsService fetchEmailsService;

    @Test
    void execute_throwsWhenLimitIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> fetchEmailsService.execute(0));
        assertThrows(IllegalArgumentException.class, () -> fetchEmailsService.execute(-1));
    }

    @Test
    void execute_throwsWhenLimitExceedsMax() {
        assertThrows(IllegalArgumentException.class, () -> fetchEmailsService.execute(51));
    }

    @Test
    void execute_cachesEmailsWithIndexes() {
        EmailMessage first = EmailMessage.builder()
            .emailId("id-1")
            .from("sender1@example.com")
            .subject("Subject 1")
            .snippet("Snippet 1")
            .receivedAt(Instant.parse("2024-01-01T00:00:00Z"))
            .build();
        EmailMessage second = EmailMessage.builder()
            .emailId("id-2")
            .from("sender2@example.com")
            .subject("Subject 2")
            .snippet("Snippet 2")
            .receivedAt(Instant.parse("2024-01-02T00:00:00Z"))
            .build();

        when(emailProvider.fetchLatest(2)).thenReturn(List.of(first, second));

        List<EmailMessage> results = fetchEmailsService.execute(2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getIndex()).isEqualTo(1);
        assertThat(results.get(1).getIndex()).isEqualTo(2);
        assertThat(fetchEmailsService.getCachedEmail(1)).isSameAs(first);
        assertThat(fetchEmailsService.getCachedEmail(2)).isSameAs(second);
    }
}
