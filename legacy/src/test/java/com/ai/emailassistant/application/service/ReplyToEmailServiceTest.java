package com.ai.emailassistant.application.service;

import com.ai.emailassistant.application.repository.EmailRepository;
import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.AIProvider;
import com.ai.emailassistant.domain.port.EmailProvider;
import com.ai.emailassistant.exception.EmailNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyToEmailServiceTest {

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private AIProvider aiProvider;

    @Mock
    private EmailRepository emailRepository;

    @InjectMocks
    private ReplyToEmailService replyToEmailService;

    @Test
    void execute_throwsWhenIndexIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> replyToEmailService.execute(0, "instruction"));
        verifyNoInteractions(emailRepository, aiProvider, emailProvider);
    }

    @Test
    void execute_throwsWhenEmailNotFound() {
        when(emailRepository.findByIndex(1)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> replyToEmailService.execute(1, "instruction"));

        verify(emailRepository).findByIndex(1);
        verifyNoInteractions(aiProvider, emailProvider);
    }

    @Test
    void execute_generatesReplyAndSendsEmail() {
        EmailMessage email = EmailMessage.builder()
            .emailId("email-123")
            .from("sender@example.com")
            .subject("Subject")
            .snippet("Snippet")
            .build();
        String longReply = "x".repeat(205);

        when(emailRepository.findByIndex(2)).thenReturn(Optional.of(email));
        when(aiProvider.generateReply(email, "follow up"))
            .thenReturn(longReply);

        String preview = replyToEmailService.execute(2, "follow up");

        verify(emailRepository).findByIndex(2);
        verify(aiProvider).generateReply(email, "follow up");
        verify(emailProvider).reply("email-123", longReply);
        assertThat(preview).isEqualTo(longReply.substring(0, EmailConstants.REPLY_PREVIEW_LENGTH) + "...");
    }
}
