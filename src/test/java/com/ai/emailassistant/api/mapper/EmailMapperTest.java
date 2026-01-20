package com.ai.emailassistant.api.mapper;

import com.ai.emailassistant.api.dto.response.EmailDto;
import com.ai.emailassistant.domain.model.EmailMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMapperTest {

    private final EmailMapper mapper = new EmailMapper();

    @Test
    void toDto_convertsEmailMessageToEmailDto() {
        EmailMessage email = EmailMessage.builder()
            .index(1)
            .emailId("email-123")
            .from("sender@example.com")
            .subject("Test Subject")
            .snippet("Test snippet")
            .receivedAt(Instant.parse("2024-01-01T00:00:00Z"))
            .build();

        EmailDto dto = mapper.toDto(email);

        assertThat(dto.getIndex()).isEqualTo(1);
        assertThat(dto.getEmailId()).isEqualTo("email-123");
        assertThat(dto.getFrom()).isEqualTo("sender@example.com");
        assertThat(dto.getSubject()).isEqualTo("Test Subject");
        assertThat(dto.getSnippet()).isEqualTo("Test snippet");
        assertThat(dto.getReceivedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void toDto_returnsNullWhenInputIsNull() {
        EmailDto dto = mapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toDtoList_convertsListOfEmailMessages() {
        EmailMessage email1 = EmailMessage.builder()
            .index(1)
            .emailId("email-1")
            .from("sender1@example.com")
            .subject("Subject 1")
            .snippet("Snippet 1")
            .receivedAt(Instant.now())
            .build();

        EmailMessage email2 = EmailMessage.builder()
            .index(2)
            .emailId("email-2")
            .from("sender2@example.com")
            .subject("Subject 2")
            .snippet("Snippet 2")
            .receivedAt(Instant.now())
            .build();

        List<EmailDto> dtos = mapper.toDtoList(List.of(email1, email2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getEmailId()).isEqualTo("email-1");
        assertThat(dtos.get(1).getEmailId()).isEqualTo("email-2");
    }

    @Test
    void toDtoList_returnsNullWhenInputIsNull() {
        List<EmailDto> dtos = mapper.toDtoList(null);
        assertThat(dtos).isNull();
    }
}
