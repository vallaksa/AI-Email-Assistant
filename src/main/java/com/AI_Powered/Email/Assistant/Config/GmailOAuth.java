package com.AI_Powered.Email.Assistant.Config;

import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GmailOAuth {
    private static final Logger logger = LoggerFactory.getLogger(GmailOAuth.class);
    private static final String APPLICATION_NAME = "AI Email Assistant";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://mail.google.com/");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public static Credential getCredentials() {
        try {
            // Check tokens directory exists and create if not
            Path tokensPath = Paths.get(TOKENS_DIRECTORY_PATH);
            if (!Files.exists(tokensPath)) {
                logger.info("Creating tokens directory at: {}", tokensPath.toAbsolutePath());
                Files.createDirectories(tokensPath);
            }
            
            // Setup transport
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load client secrets
            InputStream in = GmailOAuth.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                logger.error("Resource not found: {}", CREDENTIALS_FILE_PATH);
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            
            logger.info("Loading client secrets from credentials.json");
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            
            // Check for required fields in client secrets
            if (clientSecrets.getDetails().getClientId() == null || 
                clientSecrets.getDetails().getClientId().isEmpty() ||
                clientSecrets.getDetails().getClientSecret() == null || 
                clientSecrets.getDetails().getClientSecret().isEmpty()) {
                logger.error("Invalid client secrets: missing client_id or client_secret");
                throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                    "Invalid client secrets: missing client_id or client_secret");
            }
            
            // Build flow and trigger user authorization request
            logger.info("Building authorization flow");
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .setApprovalPrompt("force") // Force to get refresh token every time
                    .build();

            logger.info("Starting local server receiver for OAuth callback");
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .setCallbackPath("/Callback")
                    .build();
                    
            logger.info("Authorizing...");
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            logger.info("Successfully obtained credentials");
            return credential;
        } catch (EmailAssistantException e) {
            // Rethrow EmailAssistantException without wrapping
            throw e;
        } catch (IOException e) {
            logger.error("IO error when getting credentials: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                "IO error when getting credentials: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            logger.error("Security error when getting credentials: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                "Security error when getting credentials: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error when getting credentials: {}", e.getMessage());
            throw new EmailAssistantException(ErrorCode.AUTHENTICATION_ERROR, 
                "Unexpected error when getting credentials: " + e.getMessage(), e);
        }
    }
}
