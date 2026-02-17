package com.ai.emailassistant.api.controller;

import com.ai.emailassistant.api.dto.request.ReplyToEmailRequest;
import com.ai.emailassistant.api.dto.request.ReplyToEmailByIdRequest;
import com.ai.emailassistant.api.dto.response.ApiResponse;
import com.ai.emailassistant.api.dto.response.EmailDto;
import com.ai.emailassistant.api.dto.response.ThreadMessageDto;
import com.ai.emailassistant.api.mapper.EmailMapper;
import com.ai.emailassistant.application.service.FetchEmailsService;
import com.ai.emailassistant.application.service.FetchSentEmailsService;
import com.ai.emailassistant.application.service.FetchEmailThreadService;
import com.ai.emailassistant.application.service.GetEmailService;
import com.ai.emailassistant.application.service.ReplyToEmailService;
import com.ai.emailassistant.common.ApiConstants;
import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.infrastructure.email.config.GmailProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller (Thin layer - no business logic).
 * Routes requests to application services and formats responses.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH)
@RequiredArgsConstructor
public class EmailController {
    private final FetchEmailsService fetchEmailsService;
    private final FetchSentEmailsService fetchSentEmailsService;
    private final GetEmailService getEmailService;
    private final FetchEmailThreadService fetchEmailThreadService;
    private final ReplyToEmailService replyToEmailService;
    private final EmailMapper emailMapper;
    private final GmailProperties gmailProperties;

    /**
     * POST /api/emails/fetch/{limit}
     * Fetch the most recent emails.
     */
    @PostMapping(ApiConstants.FETCH_EMAILS_PATH + "/{limit}")
    public ResponseEntity<ApiResponse<List<EmailDto>>> fetchEmails(
        @PathVariable int limit
    ) {
        int resolvedLimit = limit > 0 ? limit : EmailConstants.DEFAULT_EMAIL_LIMIT;
        log.info("Received request to fetch emails with limit: {}", resolvedLimit);

        List<EmailDto> emailDtos = emailMapper.toDtoList(
            fetchEmailsService.execute(resolvedLimit)
        );

        return ResponseEntity.ok(
            ApiResponse.ok(ApiConstants.MSG_EMAILS_FETCHED, emailDtos)
        );
    }

    /**
     * POST /api/emails/sent/{limit}
     * Fetch the most recent sent emails.
     */
    @PostMapping(ApiConstants.FETCH_SENT_EMAILS_PATH + "/{limit}")
    public ResponseEntity<ApiResponse<List<EmailDto>>> fetchSentEmails(
        @PathVariable int limit
    ) {
        int resolvedLimit = limit > 0 ? limit : EmailConstants.DEFAULT_EMAIL_LIMIT;
        log.info("Received request to fetch sent emails with limit: {}", resolvedLimit);

        List<EmailDto> emailDtos = emailMapper.toDtoList(
            fetchSentEmailsService.execute(resolvedLimit)
        );

        return ResponseEntity.ok(
            ApiResponse.ok(ApiConstants.MSG_SENT_EMAILS_FETCHED, emailDtos)
        );
    }

    /**
     * GET /api/emails/{emailId}
     * Get a single email by its ID.
     */
    @GetMapping("/{emailId}")
    public ResponseEntity<ApiResponse<EmailDto>> getEmail(
        @PathVariable String emailId
    ) {
        log.info("Received request to get email: {}", emailId);

        EmailDto emailDto = emailMapper.toDto(
            getEmailService.execute(emailId)
        );

        return ResponseEntity.ok(
            ApiResponse.ok("Email retrieved successfully", emailDto)
        );
    }

    /**
     * GET /api/emails/{emailId}/thread
     * Get all messages in an email thread.
     */
    @GetMapping("/{emailId}/thread")
    public ResponseEntity<ApiResponse<List<ThreadMessageDto>>> getThread(
        @PathVariable String emailId
    ) {
        log.info("Received request to get thread for email: {}", emailId);

        // First get the email to find its threadId
        EmailDto emailDto = emailMapper.toDto(
            getEmailService.execute(emailId)
        );

        if (emailDto.getThreadId() == null || emailDto.getThreadId().isBlank()) {
            return ResponseEntity.ok(
                ApiResponse.ok("No thread found", List.of())
            );
        }

        // Fetch thread messages
        List<ThreadMessageDto> threadMessages = emailMapper.toThreadMessageDtoList(
            fetchEmailThreadService.execute(emailDto.getThreadId()),
            gmailProperties.getAccount().getEmail()
        );

        return ResponseEntity.ok(
            ApiResponse.ok("Thread retrieved successfully", threadMessages)
        );
    }

    /**
     * POST /api/emails/reply
     * Generate and send an AI reply to a selected email (by index).
     */
    @PostMapping(ApiConstants.REPLY_EMAIL_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> replyToEmail(
        @Valid @RequestBody ReplyToEmailRequest request
    ) {
        log.info("Received request to reply to email at index: {}", request.getIndex());

        String replyPreview = replyToEmailService.execute(
            request.getIndex(),
            request.getUserInstruction()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("index", request.getIndex());
        data.put("replyPreview", replyPreview);

        return ResponseEntity.ok(
            ApiResponse.ok(ApiConstants.MSG_REPLY_SENT, data)
        );
    }

    /**
     * POST /api/emails/reply/by-id
     * Generate and send an AI reply to a selected email (by emailId).
     */
    @PostMapping("/reply/by-id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> replyToEmailById(
        @Valid @RequestBody ReplyToEmailByIdRequest request
    ) {
        log.info("Received request to reply to email: {}", request.getEmailId());

        String replyPreview = replyToEmailService.executeByEmailId(
            request.getEmailId(),
            request.getUserInstruction()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("emailId", request.getEmailId());
        data.put("replyPreview", replyPreview);

        return ResponseEntity.ok(
            ApiResponse.ok(ApiConstants.MSG_REPLY_SENT, data)
        );
    }
}
