package com.AI_Powered.Email.Assistant.AIEmailAssitService;

import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Mock implementation of email fetching functionality for testing.
 * This class provides mock email data without connecting to a real email server.
 */
@Service
public class MockEmailFetcher {
    private static final Logger logger = LoggerFactory.getLogger(MockEmailFetcher.class);
    
    private final JavaMailSender mailSender;
    
    public MockEmailFetcher(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        logger.info("MockEmailFetcher initialized");
    }
    
    /**
     * Fetch mock email messages for testing.
     * 
     * @param numberOfEmails The number of mock emails to generate
     * @return An array of mock email messages
     */
    public Message[] fetchEmails(int numberOfEmails) {
        logger.info("MOCK: Fetching {} mock emails", numberOfEmails);
        
        if (numberOfEmails <= 0 || numberOfEmails > 50) {
            logger.error("Invalid number of emails requested: {}", numberOfEmails);
            throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, 
                "Number of emails must be between 1 and 50");
        }
        
        try {
            return createMockMessages(numberOfEmails);
        } catch (MessagingException e) {
            logger.error("Error creating mock messages: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.EMAIL_FETCH_ERROR, 
                "Error creating mock messages: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create mock email messages with test data.
     * 
     * @param count The number of mock messages to create
     * @return An array of mock email messages
     * @throws MessagingException If there's an error creating the messages
     */
    private Message[] createMockMessages(int count) throws MessagingException {
        logger.info("Creating {} mock messages", count);
        MimeMessage[] messages = new MimeMessage[count];
        
        for (int i = 0; i < count; i++) {
            MimeMessage message = (MimeMessage) mailSender.createMimeMessage();
            message.setFrom(new InternetAddress("test" + i + "@example.com"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress("you@example.com"));
            message.setSubject("Test Email " + (i + 1));
            message.setText("This is test email " + (i + 1) + " content.");
            message.setSentDate(new Date());
            
            messages[i] = message;
        }
        
        return messages;
    }
    
    /**
     * Get text content from a mock message.
     * 
     * @param message The message to extract text from
     * @return The text content of the message
     */
    public String getTextFromMessage(Message message) {
        logger.info("MOCK: Getting text from mock message");
        return "This is a mock email content for testing purposes.";
    }
} 