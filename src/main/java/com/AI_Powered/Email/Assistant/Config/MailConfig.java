package com.AI_Powered.Email.Assistant.Config;

import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import com.google.api.client.auth.oauth2.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Properties;

@Configuration
public class MailConfig {
    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);
    private static final String EMAIL_ADDRESS = "brendonstark69@gmail.com";

    @Bean
    public JavaMailSender javaMailSender() {
        logger.info("Configuring JavaMailSender with OAuth2 for Gmail");
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(EMAIL_ADDRESS);

            // Retrieve OAuth Access Token
            Credential credential = GmailOAuth.getCredentials();
            if (credential == null) {
                logger.error("Failed to obtain OAuth credentials");
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Failed to obtain OAuth credentials");
            }
            
            final String accessToken = credential.getAccessToken();
            logger.info("Retrieved access token successfully");

            // Configure mail properties
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.debug", "true"); // Enable for debugging
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            
            // Create a custom authenticator that uses XOAUTH2
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_ADDRESS, accessToken);
                }
            };
            
            // Set the session with our authenticator
            Session session = Session.getInstance(props, authenticator);
            mailSender.setSession(session);
            
            logger.info("JavaMailSender configured successfully");
            return mailSender;
        } catch (EmailAssistantException e) {
            // Rethrow EmailAssistantException without wrapping
            throw e;
        } catch (Exception e) {
            logger.error("Error configuring JavaMailSender: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                "Error configuring JavaMailSender: " + e.getMessage(), e);
        }
    }
}
