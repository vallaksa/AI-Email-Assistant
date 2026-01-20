package com.ai.emailassistant.api.controller;

import com.ai.emailassistant.api.dto.request.ReplyToEmailRequest;
import com.ai.emailassistant.api.dto.response.ApiResponse;
import com.ai.emailassistant.api.dto.response.EmailDto;
import com.ai.emailassistant.api.mapper.EmailMapper;
import com.ai.emailassistant.application.service.FetchEmailsService;
import com.ai.emailassistant.application.service.ReplyToEmailService;
import com.ai.emailassistant.common.ApiConstants;
import com.ai.emailassistant.common.EmailConstants;
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
    private final ReplyToEmailService replyToEmailService;
    private final EmailMapper emailMapper;

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
     * POST /api/emails/reply
     * Generate and send an AI reply to a selected email.
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
}
