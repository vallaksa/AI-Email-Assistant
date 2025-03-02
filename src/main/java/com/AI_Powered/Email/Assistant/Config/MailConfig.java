package com.AI_Powered.Email.Assistant.Config;

import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Properties;

@Configuration
@Profile("!test") // Only active when 'test' profile is NOT active
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final String TOKEN_DIRECTORY_PATH = "tokens";
    private final EmailProperties emailProperties;
    private final Environment environment;

    @Autowired
    public MailConfig(EmailProperties emailProperties, Environment environment) {
        this.emailProperties = emailProperties;
        this.environment = environment;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        try {
            // Skip real configuration if we're in test mode
            if (environment.matchesProfiles("test")) {
                logger.info("Test profile detected, skipping real JavaMailSender configuration");
                return null; // This will be overridden by the TestConfig bean
            }
            
            String emailAddress = emailProperties.getAccount().getAddress();
            String smtpHost = emailProperties.getSmtp().getHost();
            int smtpPort = emailProperties.getSmtp().getPort();
            String sslTrust = emailProperties.getSmtp().getSslTrust();

            logger.info("Configuring JavaMailSender with OAuth2 for Gmail");

            // Create JavaMailSenderImpl with our properties
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(smtpHost);
            mailSender.setPort(smtpPort);
            mailSender.setUsername(emailAddress);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");
            if (sslTrust != null && !sslTrust.isEmpty()) {
                props.put("mail.smtp.ssl.trust", sslTrust);
            }
            
            // Retrieve OAuth Access Token
            Credential credential = GmailOAuth.getCredentials();
            if (credential == null) {
                logger.error("Failed to obtain OAuth credentials");
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, "Failed to obtain OAuth credentials");
            }
            
            final String accessToken = credential.getAccessToken();
            logger.info("Retrieved access token successfully");
            
            // Create a custom authenticator that uses XOAUTH2
            final String finalEmailAddress = emailAddress;
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalEmailAddress, accessToken);
                }
            };
            
            // Set the session with our authenticator
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            Session session = Session.getInstance(props, authenticator);
            mailSender.setSession(session);
            
            logger.info("JavaMailSender configured successfully");
            return mailSender;
        } catch (Exception e) {
            logger.error("Error configuring JavaMailSender: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.CONFIGURATION_ERROR, "Failed to configure mail sender", e);
        }
    }
}
