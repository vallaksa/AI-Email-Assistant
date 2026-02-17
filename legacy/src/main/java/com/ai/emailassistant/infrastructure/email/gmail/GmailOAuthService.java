package com.ai.emailassistant.infrastructure.email.gmail;

import com.ai.emailassistant.common.EmailConstants;
import com.ai.emailassistant.infrastructure.email.config.GmailProperties;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * Service for handling Gmail OAuth2 authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GmailOAuthService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_SEND
    );

    private final GmailProperties gmailProperties;

    /**
     * Initialize and return Gmail service instance.
     * Returns null if credentials are missing (test mode).
     *
     * @return Gmail service instance, or null if credentials are missing (test mode)
     */
    public Gmail initializeGmailService() {
        log.info("Initializing Gmail service for: {}", gmailProperties.getAccount().getEmail());

        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            File credFile = new File(EmailConstants.GMAIL_CREDENTIALS_FILE);

            if (!credFile.exists()) {
                log.warn("Gmail credentials file not found: {}. Running in test mode with mock emails.", 
                    EmailConstants.GMAIL_CREDENTIALS_FILE);
                return null;
            }

            GoogleClientSecrets secrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new FileReader(credFile));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, secrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(EmailConstants.GMAIL_TOKENS_DIR)))
                    .setAccessType("offline")
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(
                    flow,
                    new LocalServerReceiver.Builder().setPort(EmailConstants.GMAIL_OAUTH_PORT).build()
            ).authorize("user");

            Gmail gmailService = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("Email-Assistant")
                    .build();

            log.info("Gmail service initialized successfully");
            return gmailService;

        } catch (Exception e) {
            log.error("Failed to initialize Gmail service", e);
            log.warn("Falling back to test mode due to initialization error");
            return null;
        }
    }
}
