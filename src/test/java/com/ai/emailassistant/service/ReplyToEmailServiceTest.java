package com.ai.emailassistant.service;

import com.ai.emailassistant.model.EmailMessage;
import com.ai.emailassistant.model.ports.AIProvider;
import com.ai.emailassistant.model.ports.EmailProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private FetchEmailsService fetchEmailsService;

    @InjectMocks
    private ReplyToEmailService replyToEmailService;

    @Test
    void execute_throwsWhenIndexIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> replyToEmailService.execute(0, "instruction"));
        verifyNoInteractions(fetchEmailsService, aiProvider, emailProvider);
    }

    @Test
    void execute_throwsWhenEmailNotCached() {
        when(fetchEmailsService.getCachedEmail(1)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> replyToEmailService.execute(1, "instruction"));

        verify(fetchEmailsService).getCachedEmail(1);
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

        when(fetchEmailsService.getCachedEmail(2)).thenReturn(email);
        when(aiProvider.generateReply(email, "follow up"))
            .thenReturn(longReply);

        String preview = replyToEmailService.execute(2, "follow up");

        verify(fetchEmailsService).getCachedEmail(2);
        verify(aiProvider).generateReply(email, "follow up");
        verify(emailProvider).reply("email-123", longReply);
        assertThat(preview).isEqualTo(longReply.substring(0, 200) + "...");
    }
}
