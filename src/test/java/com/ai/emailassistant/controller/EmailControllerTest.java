package com.ai.emailassistant.controller;

import com.ai.emailassistant.model.EmailMessage;
import com.ai.emailassistant.model.RequestResponse.AIRequest;
import com.ai.emailassistant.service.FetchEmailsService;
import com.ai.emailassistant.service.ReplyToEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FetchEmailsService fetchEmailsService;

    @MockBean
    private ReplyToEmailService replyToEmailService;

    @Test
    void fetchEmails_defaultsLimitWhenNonPositive() throws Exception {
        EmailMessage email = EmailMessage.builder()
            .index(1)
            .emailId("email-1")
            .from("sender@example.com")
            .subject("Subject")
            .snippet("Snippet")
            .receivedAt(Instant.parse("2024-01-01T00:00:00Z"))
            .build();

        when(fetchEmailsService.execute(10)).thenReturn(List.of(email));

        mockMvc.perform(post("/api/emails/fetch/0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Successfully fetched emails"))
            .andExpect(jsonPath("$.data[0].emailId").value("email-1"));

        verify(fetchEmailsService).execute(10);
    }

    @Test
    void replyToEmail_returnsPreviewPayload() throws Exception {
        when(replyToEmailService.execute(3, "Be concise"))
            .thenReturn("Preview text");

        AIRequest request = new AIRequest(3, "Be concise");

        mockMvc.perform(post("/api/emails/reply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Reply sent successfully"))
            .andExpect(jsonPath("$.data.index").value(3))
            .andExpect(jsonPath("$.data.replyPreview").value("Preview text"));

        verify(replyToEmailService).execute(3, "Be concise");
    }
}
