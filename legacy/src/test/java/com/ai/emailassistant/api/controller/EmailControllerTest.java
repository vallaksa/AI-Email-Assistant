package com.ai.emailassistant.api.controller;

import com.ai.emailassistant.api.dto.request.ReplyToEmailRequest;
import com.ai.emailassistant.api.mapper.EmailMapper;
import com.ai.emailassistant.application.service.FetchEmailsService;
import com.ai.emailassistant.application.service.ReplyToEmailService;
import com.ai.emailassistant.common.ApiConstants;
import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
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

    @MockBean
    private EmailMapper emailMapper;

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

        when(fetchEmailsService.execute(EmailConstants.DEFAULT_EMAIL_LIMIT))
            .thenReturn(List.of(email));
        when(emailMapper.toDtoList(List.of(email)))
            .thenReturn(List.of(com.ai.emailassistant.api.dto.response.EmailDto.builder()
                .index(1)
                .emailId("email-1")
                .from("sender@example.com")
                .subject("Subject")
                .snippet("Snippet")
                .receivedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build()));

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + ApiConstants.FETCH_EMAILS_PATH + "/0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(ApiConstants.MSG_EMAILS_FETCHED))
            .andExpect(jsonPath("$.data[0].emailId").value("email-1"));

        verify(fetchEmailsService).execute(EmailConstants.DEFAULT_EMAIL_LIMIT);
    }

    @Test
    void replyToEmail_returnsPreviewPayload() throws Exception {
        when(replyToEmailService.execute(3, "Be concise"))
            .thenReturn("Preview text");

        ReplyToEmailRequest request = new ReplyToEmailRequest(3, "Be concise");

        mockMvc.perform(post(ApiConstants.API_BASE_PATH + ApiConstants.REPLY_EMAIL_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(ApiConstants.MSG_REPLY_SENT))
            .andExpect(jsonPath("$.data.index").value(3))
            .andExpect(jsonPath("$.data.replyPreview").value("Preview text"));

        verify(replyToEmailService).execute(3, "Be concise");
    }
}
