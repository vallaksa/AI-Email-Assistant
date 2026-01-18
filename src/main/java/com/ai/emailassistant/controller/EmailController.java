package com.ai.emailassistant.controller;

import com.ai.emailassistant.service.FetchEmailsService;
import com.ai.emailassistant.service.ReplyToEmailService;
import com.ai.emailassistant.model.RequestResponse.EmailResponse;
import com.ai.emailassistant.model.EmailMessage;
import com.ai.emailassistant.model.RequestResponse.AIRequest;
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
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final FetchEmailsService fetchEmailsService;
    private final ReplyToEmailService replyToEmailService;

    /**
     * POST /api/emails/fetch
     * Fetch the most recent emails.
     */
    @PostMapping("/fetch/{limit}")
    public ResponseEntity<EmailResponse<List<EmailMessage>>> fetchEmails(
        @PathVariable int limit
    ) {
        int resolvedLimit = limit > 0 ? limit : 10;
        log.info("Received request to fetch emails with limit: {}", resolvedLimit);

        List<EmailMessage> emails = fetchEmailsService.execute(resolvedLimit);

        return ResponseEntity.ok(
                EmailResponse.ok("Successfully fetched emails", emails)
        );
    }

    /**
     * POST /api/emails/reply
     * Generate and send an AI reply to a selected email.
     */
    @PostMapping("/reply")
    public ResponseEntity<EmailResponse<Map<String, Object>>> replyToEmail(
        @RequestBody AIRequest request
    ) {
        log.info("Received request to reply to email.");

        String replyPreview = replyToEmailService.execute(
            request.getIndex(),
            request.getUserInstruction()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("index", request.getIndex());
        data.put("replyPreview", replyPreview);

        return ResponseEntity.ok(
            EmailResponse.ok("Reply sent successfully", data)
        );
    }
}
