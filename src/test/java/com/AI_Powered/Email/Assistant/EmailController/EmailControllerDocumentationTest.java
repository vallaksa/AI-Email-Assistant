package com.AI_Powered.Email.Assistant.EmailController;

import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailFetcher;
import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailSenderService;
import com.AI_Powered.Email.Assistant.model.EmailResponse;
import com.AI_Powered.Email.Assistant.model.ReplyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
public class EmailControllerDocumentationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailSenderService emailSenderService;

    @Mock
    private Message mockMessage;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void fetchEmailsTest() throws Exception {
        // Setup mock behavior for EmailFetcher
        Message[] mockMessages = new Message[]{mockMessage};
        
        try (MockedStatic<EmailFetcher> mockedEmailFetcher = Mockito.mockStatic(EmailFetcher.class)) {
            mockedEmailFetcher.when(() -> EmailFetcher.fetchEmails(anyInt())).thenReturn(mockMessages);
            
            // Mock message attributes
            when(mockMessage.getFrom()).thenReturn(new jakarta.mail.Address[]{new jakarta.mail.internet.InternetAddress("test@example.com")});
            when(mockMessage.getSubject()).thenReturn("Test Email Subject");
            when(mockMessage.getSentDate()).thenReturn(new java.util.Date());
            
            // Perform the test
            this.mockMvc.perform(get("/email/fetch")
                    .param("count", "5")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(document("fetch-emails",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("count").description("Number of emails to fetch (1-50)")
                            ),
                            responseFields(
                                    fieldWithPath("[].from").description("Email sender address"),
                                    fieldWithPath("[].subject").description("Email subject line"),
                                    fieldWithPath("[].date").description("Date when the email was received")
                            )
                    ));
        }
    }

    @Test
    public void replyToEmailTest() throws Exception {
        // Setup mock behavior for EmailFetcher
        Message[] mockMessages = new Message[]{mockMessage};
        
        try (MockedStatic<EmailFetcher> mockedEmailFetcher = Mockito.mockStatic(EmailFetcher.class)) {
            mockedEmailFetcher.when(() -> EmailFetcher.fetchEmails(anyInt())).thenReturn(mockMessages);
            
            // Mock message attributes
            when(mockMessage.getFrom()).thenReturn(new jakarta.mail.Address[]{new jakarta.mail.internet.InternetAddress("test@example.com")});
            doNothing().when(emailSenderService).replyToEmail(any(Message.class));
            
            ReplyRequest replyRequest = new ReplyRequest("This is a test reply message");
            
            // Perform the test
            this.mockMvc.perform(post("/email/reply")
                    .param("emailIndex", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(replyRequest))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(document("reply-to-email",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("emailIndex").description("Index of the email to reply to (1-based)")
                            ),
                            requestFields(
                                    fieldWithPath("message").description("Custom message to include in the reply").optional()
                            ),
                            responseFields(
                                    fieldWithPath("status").description("Operation status (success or error)"),
                                    fieldWithPath("message").description("Response message"),
                                    fieldWithPath("error").description("Error code (only present in error responses)").optional()
                            )
                    ));
        }
    }
} 