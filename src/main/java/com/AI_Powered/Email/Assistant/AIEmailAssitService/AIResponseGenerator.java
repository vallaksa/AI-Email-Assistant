package com.AI_Powered.Email.Assistant.AIEmailAssitService;

import com.AI_Powered.Email.Assistant.Config.MockAIResponseGeneratorConfig;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Service for generating AI-powered responses to emails.
 * In test mode, it returns a mock response instead of calling the actual AI model.
 */
@Service
public class AIResponseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AIResponseGenerator.class);
    private static final String MODEL_API_URL = "http://localhost:11434/api/generate";
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static Environment environment;
    
    // Constructor to get access to the Spring environment
    public AIResponseGenerator(Environment env) {
        environment = env;
        logger.info("AIResponseGenerator initialized with environment: {}", 
                Arrays.toString(environment.getActiveProfiles()));
    }
    
    /**
     * Generate an AI-powered response to an email.
     * 
     * @param sender The sender of the original email
     * @param subject The subject of the original email
     * @param emailBody The body of the original email
     * @return AI-generated response formatted as HTML
     */
    public static String generateResponse(String sender, String subject, String emailBody) {
        // Check if we're in test mode
        if (environment != null && environment.matchesProfiles("test")) {
            logger.info("Using mock response generator in test profile");
            return MockAIResponseGeneratorConfig.generateMockResponse(sender, subject, emailBody);
        }
        
        if (sender == null || sender.trim().isEmpty()) {
            logger.error("Sender email cannot be empty");
            throw new EmailAssistantException(ErrorCode.INVALID_REQUEST, "Sender email cannot be empty");
        }
        
        logger.info("Generating AI response for email from: {}", sender);
        
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        
        try {
            String prompt = buildPrompt(sender, subject, emailBody);
            String jsonInput = escapeJson("{ \"model\": \"mistral\", \"prompt\": \"" + prompt + "\", \"stream\": false }");

            // Set up the connection
            URL url = new URL(MODEL_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setDoOutput(true);

            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            // Check for error response
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                
                logger.error("AI service returned error code: {} with message: {}", responseCode, errorResponse);
                
                // Return fallback response instead of throwing exception
                return generateFallbackResponse(sender, subject);
            }

            // Read the response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            
            if (!rootNode.has("response")) {
                logger.error("AI service returned invalid response format: {}", response);
                return generateFallbackResponse(sender, subject);
            }
            
            String aiResponse = rootNode.get("response").asText();
            logger.info("Successfully generated AI response");
            return aiResponse;
            
        } catch (ConnectException e) {
            logger.error("Failed to connect to AI service: {}", e.getMessage());
            return generateFallbackResponse(sender, subject);
        } catch (EmailAssistantException e) {
            // Rethrow EmailAssistantException without wrapping
            throw e;
        } catch (IOException e) {
            logger.error("Failed to connect to AI service: {}", e.getMessage());
            return generateFallbackResponse(sender, subject);
        } catch (Exception e) {
            logger.error("Unexpected error generating AI response: {}", e.getMessage());
            return generateFallbackResponse(sender, subject);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Failed to close reader: {}", e.getMessage());
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    private static String generateFallbackResponse(String sender, String subject) {
        logger.info("Generating fallback response for email from: {}", sender);
        
        String senderName = extractNameFromEmail(sender);
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        
        return "<html><body>" +
               "<p>Dear " + senderName + ",</p>" +
               "<p>Thank you for your email regarding \"" + (subject != null ? subject : "your inquiry") + "\".</p>" +
               "<p>I've received your message and will get back to you with a more detailed response as soon as possible.</p>" +
               "<p>Please note that this is an automated acknowledgment as our AI assistant is currently unavailable.</p>" +
               "<p>Best regards,<br>AI Email Assistant</p>" +
               "<p><small>Generated on " + currentDate + "</small></p>" +
               "</body></html>";
    }
    
    private static String extractNameFromEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            return "Valued Customer";
        }
        
        // Try to extract name from "Name <email>" format
        if (emailAddress.contains("<") && emailAddress.contains(">")) {
            String name = emailAddress.substring(0, emailAddress.indexOf("<")).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        
        // Otherwise use the part before @ as name
        if (emailAddress.contains("@")) {
            String username = emailAddress.substring(0, emailAddress.indexOf("@"));
            // Capitalize first letter and replace dots/underscores with spaces
            username = username.replaceAll("[._]", " ");
            if (username.length() > 0) {
                return Character.toUpperCase(username.charAt(0)) + 
                       (username.length() > 1 ? username.substring(1) : "");
            }
        }
        
        return "Valued Customer";
    }
    
    private static String buildPrompt(String sender, String subject, String emailBody) {
        return "Write a professional email reply to the following:\n" +
                "From: " + sender + "\n" +
                "Subject: " + (subject != null ? subject : "No Subject") + "\n" +
                "Message: " + (emailBody != null ? emailBody : "");
    }
    
    private static String escapeJson(String json) {
        return json.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
