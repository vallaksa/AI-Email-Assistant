package com.AI_Powered.Email.Assistant.EmailController;

import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailFetcher;
import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailSenderService;
import com.AI_Powered.Email.Assistant.AIEmailAssitService.MockEmailFetcher;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import com.AI_Powered.Email.Assistant.model.EmailResponse;
import com.AI_Powered.Email.Assistant.model.ReplyRequest;
import jakarta.mail.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/email")
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private final EmailSenderService emailSenderService;
    private final Environment environment;
    private final MockEmailFetcher mockEmailFetcher;

    @Autowired
    public EmailController(EmailSenderService emailSenderService, Environment environment, MockEmailFetcher mockEmailFetcher) {
        this.emailSenderService = emailSenderService;
        this.environment = environment;
        this.mockEmailFetcher = mockEmailFetcher;
        logger.info("EmailController initialized with environment: {}", 
                environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default");
    }

    @GetMapping("/fetch")
    public ResponseEntity<Object> fetchEmails(
        @RequestParam(defaultValue = "5") int count
    ) {
        logger.info("Request to fetch {} emails received", count);
        
        if (count <= 0 || count > 50) {
            logger.warn("Invalid email count requested: {}", count);
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error("INVALID_REQUEST", "Count must be between 1 and 50"), 
                HttpStatus.BAD_REQUEST
            );
        }
        
        try {
            Message[] messages;
            
            // Use MockEmailFetcher if in test mode
            if (environment.matchesProfiles("test")) {
                logger.info("Using MockEmailFetcher for test profile");
                messages = mockEmailFetcher.fetchEmails(count);
            } else {
                logger.info("Using real EmailFetcher");
                messages = EmailFetcher.fetchEmails(count);
            }
            
            List<EmailResponse> emailList = new ArrayList<>();
            
            for (Message msg : messages) {
                try {
                    EmailResponse email = new EmailResponse(
                        msg.getFrom()[0].toString(),
                        msg.getSubject() != null ? msg.getSubject() : "(No Subject)",
                        msg.getSentDate() != null ? msg.getSentDate().toString() : "Unknown"
                    );
                    emailList.add(email);
                } catch (Exception e) {
                    logger.error("Error parsing email: {}", e.getMessage());
                    // Skip this email and continue
                }
            }
            
            logger.info("Successfully fetched and returned {} emails", emailList.size());
            return new ResponseEntity<>(emailList, HttpStatus.OK);
        } catch (EmailAssistantException e) {
            // Log already done in the service layer
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error(e.getErrorCode().name(), e.getMessage()),
                e.getErrorCode() == ErrorCode.INVALID_REQUEST ? 
                    HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            logger.error("Unexpected error fetching emails: {}", e.getMessage());
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error("UNEXPECTED_ERROR", "An unexpected error occurred while fetching emails"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // New endpoint with path variable
    @GetMapping("/fetch/{count}")
    public ResponseEntity<Object> fetchEmailsByPath(
        @PathVariable int count
    ) {
        logger.info("Request to fetch {} emails received via path variable", count);
        // Reuse the same logic as the query parameter endpoint
        return fetchEmails(count);
    }

    @PostMapping("/reply")
    public ResponseEntity<Object> replyToEmail(
        @RequestParam int emailIndex,
        @RequestBody(required = false) ReplyRequest replyRequest
    ) {
        logger.info("Request to reply to email at index {} received", emailIndex);
        
        if (emailIndex < 1) {
            logger.warn("Invalid email index: {}", emailIndex);
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error("INVALID_INDEX", "Email index must be a positive number"), 
                HttpStatus.BAD_REQUEST
            );
        }
        
        try {
            Message[] messages;
            
            // Use MockEmailFetcher if in test mode
            if (environment.matchesProfiles("test")) {
                logger.info("Using MockEmailFetcher for test profile");
                messages = mockEmailFetcher.fetchEmails(Math.max(10, emailIndex));
            } else {
                logger.info("Using real EmailFetcher");
                messages = EmailFetcher.fetchEmails(Math.max(10, emailIndex));
            }
            
            if (emailIndex > messages.length) {
                logger.warn("Email index {} out of range (max: {})", emailIndex, messages.length);
                return new ResponseEntity<>(
                    com.AI_Powered.Email.Assistant.model.ApiResponse.error("INDEX_OUT_OF_RANGE", "Email index out of range. Maximum available: " + messages.length),
                    HttpStatus.BAD_REQUEST
                );
            }

            Message emailToReply = messages[messages.length - emailIndex];
            emailSenderService.replyToEmail(emailToReply);
            
            logger.info("Successfully replied to email at index {}", emailIndex);
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.success("Reply sent to: " + emailToReply.getFrom()[0]),
                HttpStatus.OK
            );
        } catch (EmailAssistantException e) {
            // Log already done in the service layer
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error(e.getErrorCode().name(), e.getMessage()),
                e.getErrorCode() == ErrorCode.INVALID_REQUEST ? 
                    HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            logger.error("Unexpected error replying to email: {}", e.getMessage());
            return new ResponseEntity<>(
                com.AI_Powered.Email.Assistant.model.ApiResponse.error("UNEXPECTED_ERROR", "An unexpected error occurred while replying to the email"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // New endpoint with path variable for replying to emails
    @PostMapping("/reply/{emailIndex}")
    public ResponseEntity<Object> replyToEmailByPath(
        @PathVariable int emailIndex,
        @RequestBody(required = false) ReplyRequest replyRequest
    ) {
        logger.info("Request to reply to email at index {} received via path variable", emailIndex);
        // Reuse the same logic as the query parameter endpoint
        return replyToEmail(emailIndex, replyRequest);
    }
}
