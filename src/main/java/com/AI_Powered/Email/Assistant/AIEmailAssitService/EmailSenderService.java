package com.AI_Powered.Email.Assistant.AIEmailAssitService;

import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
    private final JavaMailSender mailSender;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            logger.info("Attempting to send email to: {}", to);
            
            if (to == null || to.trim().isEmpty()) {
                throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, "Recipient email address cannot be empty");
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Email successfully sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new EmailAssistantException(ErrorCode.EMAIL_SEND_ERROR, 
                    "Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", to, e.getMessage());
            throw new EmailAssistantException(ErrorCode.EMAIL_SEND_ERROR, 
                    "Unexpected error sending email: " + e.getMessage(), e);
        }
    }

    public void replyToEmail(Message email) {
        try {
            if (email == null) {
                throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, "Email message cannot be null");
            }
            
            String sender = email.getFrom()[0].toString();
            logger.info("Preparing reply to sender: {}", sender);
            
            String subject = "Re: " + email.getSubject();
            String emailBody = EmailFetcher.getTextFromMessage(email);

            String aiGeneratedResponse = AIResponseGenerator.generateResponse(sender, subject, emailBody);

            sendEmail(sender, subject, aiGeneratedResponse);
            logger.info("Reply successfully sent to: {}", sender);
        } catch (EmailAssistantException e) {
            // Rethrow EmailAssistantException without wrapping
            throw e;
        } catch (Exception e) {
            logger.error("Failed to reply to email: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.EMAIL_REPLY_ERROR, 
                    "Failed to reply to email: " + e.getMessage(), e);
        }
    }
}
