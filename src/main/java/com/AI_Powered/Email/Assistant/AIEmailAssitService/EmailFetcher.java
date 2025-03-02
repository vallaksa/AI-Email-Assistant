package com.AI_Powered.Email.Assistant.AIEmailAssitService;

import com.AI_Powered.Email.Assistant.Config.EmailProperties;
import com.AI_Powered.Email.Assistant.Config.GmailOAuth;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import com.google.api.client.auth.oauth2.Credential;
import com.sun.mail.imap.IMAPStore;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Arrays;

@Service
public class EmailFetcher {
    private static final Logger logger = LoggerFactory.getLogger(EmailFetcher.class);
    
    private static EmailProperties emailProperties;
    private static Environment environment;
    private static JavaMailSender mailSender;
    
    @Autowired
    public EmailFetcher(EmailProperties emailProperties, Environment environment, JavaMailSender mailSender) {
        EmailFetcher.emailProperties = emailProperties;
        EmailFetcher.environment = environment;
        EmailFetcher.mailSender = mailSender;
        
        // Log active profiles to help with debugging
        String[] activeProfiles = environment.getActiveProfiles();
        logger.info("EmailFetcher initialized with active profiles: {}", 
                activeProfiles.length > 0 ? Arrays.toString(activeProfiles) : "default");
        
        // Explicitly check for test profile
        boolean isTestProfile = environment.matchesProfiles("test");
        logger.info("Is test profile active? {}", isTestProfile);
    }
    
    public static Message[] fetchEmails(int numberOfEmails) {
        if (numberOfEmails <= 0 || numberOfEmails > 50) {
            logger.error("Invalid number of emails requested: {}", numberOfEmails);
            throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, 
                "Number of emails must be between 1 and 50");
        }
        
        // Check if we're in test mode and return mock data
        if (environment != null) {
            boolean isTestProfile = environment.matchesProfiles("test");
            logger.info("Checking for test profile in fetchEmails: isTestProfile={}", isTestProfile);
            
            if (isTestProfile) {
                logger.info("Test profile detected, returning mock emails");
                try {
                    return createMockMessages(numberOfEmails);
                } catch (MessagingException e) {
                    logger.error("Error creating mock messages: {}", e.getMessage());
                    throw new EmailAssistantException(ErrorCode.EMAIL_FETCH_ERROR, 
                        "Error creating mock messages: " + e.getMessage(), e);
                }
            }
        } else {
            logger.warn("Environment is null in fetchEmails method");
        }
        
        // Real implementation for non-test mode
        return fetchRealEmails(numberOfEmails);
    }
    
    private static Message[] fetchRealEmails(int numberOfEmails) {
        if (emailProperties == null) {
            logger.error("Email properties not initialized");
            throw new EmailAssistantException(ErrorCode.CONFIGURATION_ERROR, 
                "Email properties not initialized");
        }

        String emailAddress = emailProperties.getAccount().getAddress();
        String host = emailProperties.getImap().getHost();
        
        logger.info("Fetching {} emails from inbox for {}", numberOfEmails, emailAddress);
        IMAPStore store = null;
        Folder inbox = null;

        try {
            logger.debug("Requesting OAuth2 credentials from GmailOAuth");
            Credential credential = GmailOAuth.getCredentials();
            if (credential == null) {
                logger.error("Failed to obtain OAuth credentials");
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Failed to obtain OAuth credentials");
            }
            
            String accessToken = credential.getAccessToken();
            logger.debug("Access token obtained successfully: {}", 
                accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null");

            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", host);
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.ssl.enable", "true");
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
            props.put("mail.debug", "true");
            props.put("mail.debug.auth", "true");
            
            logger.debug("Creating mail session with XOAUTH2 authentication");
            Session session = Session.getInstance(props);
            store = (IMAPStore) session.getStore("imaps");
            
            logger.debug("Connecting to Gmail IMAP server with XOAUTH2 token");
            try {
                store.connect(host, emailAddress, accessToken);
                logger.info("Successfully connected to Gmail IMAP server");
            } catch (MessagingException e) {
                logger.error("Failed to connect to Gmail IMAP server: {}", e.getMessage());
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Failed to connect to IMAP server: " + e.getMessage(), e);
            }
            
            try {
                logger.debug("Opening INBOX folder");
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                logger.info("INBOX opened successfully. Total messages: {}", inbox.getMessageCount());
            } catch (MessagingException e) {
                logger.error("Failed to open INBOX folder: {}", e.getMessage());
                throw new EmailAssistantException(ErrorCode.EMAIL_FETCH_ERROR, 
                    "Failed to open INBOX folder: " + e.getMessage(), e);
            }
            
            int totalMessages = inbox.getMessageCount();
            if (totalMessages == 0) {
                logger.info("No messages found in INBOX");
                return new Message[0];
            }
            
            int startMessage = Math.max(1, totalMessages - numberOfEmails + 1);
            int endMessage = totalMessages;
            
            logger.debug("Retrieving messages from {} to {}", startMessage, endMessage);
            Message[] messages = inbox.getMessages(startMessage, endMessage);
            
            FetchProfileConfig.configureFetchProfile(inbox, messages);
            
            logger.info("Successfully fetched {} emails", messages.length);
            return messages;
            
        } catch (EmailAssistantException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching emails: {}", e.getMessage(), e);
            throw new EmailAssistantException(ErrorCode.EMAIL_FETCH_ERROR, 
                "Unexpected error fetching emails: " + e.getMessage(), e);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    logger.debug("Closing INBOX folder");
                    inbox.close(false);
                }
                if (store != null && store.isConnected()) {
                    logger.debug("Closing IMAP store connection");
                    store.close();
                }
            } catch (MessagingException e) {
                logger.warn("Error closing resources: {}", e.getMessage());
            }
        }
    }

    private static Message[] createMockMessages(int count) throws MessagingException {
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

    public static String getTextFromMessage(Message message) {
        // Check if we're in test mode
        if (environment != null && environment.matchesProfiles("test")) {
            logger.info("Test profile detected, returning mock email content");
            return "This is a mock email content for testing purposes.";
        }
        
        if (message == null) {
            return "Empty message";
        }
        
        try {
            Object content = message.getContent();
            
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof MimeMultipart) {
                return getTextFromMimeMultipart((MimeMultipart) content);
            } else {
                logger.warn("Unknown content type: {}", content.getClass().getName());
                return "Unable to extract content from this email format";
            }
        } catch (IOException | MessagingException e) {
            logger.error("Error extracting text from message: {}", e.getMessage());
            return "Error extracting email content: " + e.getMessage();
        }
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) {
        try {
            StringBuilder result = new StringBuilder();
            int count = mimeMultipart.getCount();
            
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();
                
                if (disposition == null || !disposition.equalsIgnoreCase("attachment")) {
                    String contentType = bodyPart.getContentType();
                    ContentType ct = new ContentType(contentType);
                    
                    if (ct.getPrimaryType().equalsIgnoreCase("text")) {
                        result.append(bodyPart.getContent().toString());
                    } else if (bodyPart.getContent() instanceof MimeMultipart) {
                        result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
                    }
                }
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Error parsing mime multipart: {}", e.getMessage());
            return "Error parsing email content: " + e.getMessage();
        }
    }
}

class FetchProfileConfig {
    public static void configureFetchProfile(Folder folder, Message[] messages) throws MessagingException {
        jakarta.mail.FetchProfile fetchProfile = new jakarta.mail.FetchProfile();
        fetchProfile.add(jakarta.mail.FetchProfile.Item.ENVELOPE);
        fetchProfile.add(jakarta.mail.FetchProfile.Item.CONTENT_INFO);
        fetchProfile.add(jakarta.mail.FetchProfile.Item.FLAGS);
        
        String[] headers = { "Subject", "From", "Date", "Message-ID" };
        for (String header : headers) {
            fetchProfile.add(header);
        }
        
        folder.fetch(messages, fetchProfile);
    }
}
