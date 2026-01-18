package com.ai.emailassistant.api;

import com.ai.emailassistant.application.FetchEmailsService;
import com.ai.emailassistant.application.ReplyToEmailService;
import com.ai.emailassistant.domain.model.ApiResponse;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.model.ReplyRequest;
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
     * GET /api/emails?limit=10
     * Fetch the most recent emails.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmailMessage>>> fetchEmails(
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        log.info("Received request to fetch emails with limit: {}", limit);

        List<EmailMessage> emails = fetchEmailsService.execute(limit);

        return ResponseEntity.ok(
            ApiResponse.ok("Successfully fetched emails", emails)
        );
    }

    /**
     * POST /api/emails/reply
     * Generate and send an AI reply to a selected email.
     */
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<Map<String, Object>>> replyToEmail(
        @RequestBody ReplyRequest request
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
            ApiResponse.ok("Reply sent successfully", data)
        );
    }
}
