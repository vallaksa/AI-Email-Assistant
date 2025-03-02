package com.AI_Powered.Email.Assistant.AIEmailAssitService;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class EmailFetcher {
    private static final Logger logger = LoggerFactory.getLogger(EmailFetcher.class);
    private static final String EMAIL_ADDRESS = "brendonstark69@gmail.com";
    private static final String HOST = "imap.gmail.com";

    public static Message[] fetchEmails(int numberOfEmails) {
        if (numberOfEmails <= 0 || numberOfEmails > 50) {
            logger.error("Invalid number of emails requested: {}", numberOfEmails);
            throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, 
                "Number of emails must be between 1 and 50");
        }

        logger.info("Fetching {} emails from inbox", numberOfEmails);
        IMAPStore store = null;
        Folder inbox = null;

        try {
            // Get OAuth credentials
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

            // Set up properties for the mail session
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", HOST);
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.ssl.enable", "true");
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
            props.put("mail.debug", "true");
            props.put("mail.debug.auth", "true");
            
            logger.debug("Creating mail session with XOAUTH2 authentication");
            Session session = Session.getInstance(props);
            store = (IMAPStore) session.getStore("imaps");
            
            // Connect with OAuth
            logger.debug("Connecting to Gmail IMAP server with XOAUTH2 token");
            try {
                store.connect(HOST, EMAIL_ADDRESS, accessToken);
                logger.info("Successfully connected to Gmail IMAP server");
            } catch (MessagingException e) {
                logger.error("Failed to connect to Gmail IMAP server: {}", e.getMessage());
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Failed to connect to IMAP server: " + e.getMessage(), e);
            }
            
            // Open the inbox folder
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
            
            // Get messages
            int totalMessages = inbox.getMessageCount();
            if (totalMessages == 0) {
                logger.info("No messages found in INBOX");
                return new Message[0];
            }
            
            // Calculate the range of messages to retrieve
            int startMessage = Math.max(1, totalMessages - numberOfEmails + 1);
            int endMessage = totalMessages;
            
            logger.debug("Retrieving messages from {} to {}", startMessage, endMessage);
            Message[] messages = inbox.getMessages(startMessage, endMessage);
            
            // Ensure the messages are fully loaded
            FetchProfileConfig.configureFetchProfile(inbox, messages);
            
            logger.info("Successfully fetched {} emails", messages.length);
            return messages;
            
        } catch (EmailAssistantException e) {
            // Rethrow EmailAssistantException without wrapping
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching emails: {}", e.getMessage(), e);
            throw new EmailAssistantException(ErrorCode.EMAIL_FETCH_ERROR, 
                "Unexpected error fetching emails: " + e.getMessage(), e);
        } finally {
            // Close resources
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

    public static String getTextFromMessage(Message message) {
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

// Helper class to configure fetch profile
class FetchProfileConfig {
    public static void configureFetchProfile(Folder folder, Message[] messages) throws MessagingException {
        // Configure which headers to prefetch
        jakarta.mail.FetchProfile fetchProfile = new jakarta.mail.FetchProfile();
        fetchProfile.add(jakarta.mail.FetchProfile.Item.ENVELOPE);
        fetchProfile.add(jakarta.mail.FetchProfile.Item.CONTENT_INFO);
        fetchProfile.add(jakarta.mail.FetchProfile.Item.FLAGS);
        
        // Add specific headers
        String[] headers = { "Subject", "From", "Date", "Message-ID" };
        for (String header : headers) {
            fetchProfile.add(header);
        }
        
        // Fetch the configured items
        folder.fetch(messages, fetchProfile);
    }
}
