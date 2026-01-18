package com.ai.emailassistant.infrastructure.ai;

import com.ai.emailassistant.model.EmailMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Generic HTTP-based AI provider driven entirely by configuration.
 */
@Slf4j
@Component
public class AIProvider implements com.ai.emailassistant.model.ports.AIProvider {

    private HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.api.url}")
    private String apiUrl;
    @Value("${ai.api.key:}")
    private String apiKey;
    @Value("${ai.api.auth-header:}")
    private String authHeader;
    @Value("${ai.api.auth-prefix:}")
    private String authPrefix;
    @Value("${ai.api.content-type:application/json}")
    private String contentType;
    @Value("${ai.model.name:}")
    private String modelName;
    @Value("${ai.timeouts.connect-ms:10000}")
    private Integer connectTimeoutMs;
    @Value("${ai.timeouts.read-ms:60000}")
    private Integer readTimeoutMs;

    @PostConstruct
    void initHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
    }

    @Override
    public String generateReply(EmailMessage email, String userInstruction) {
        log.info("Generating reply using configured AI provider at {}", apiUrl);

        try {
            String prompt = PromptBuilder.buildPrompt(email, userInstruction);
            String body = buildRequestBody(prompt);
            JsonNode response = callApi(body);
            String reply = extractReply(response);

            if (reply == null || reply.isBlank()) {
                throw new RuntimeException("AI provider returned empty response");
            }

            String cleaned = reply.trim();
            log.info("Generated reply ({} chars)", cleaned.length());
            return cleaned;
        } catch (IOException e) {
            log.error("IOException connecting to AI provider at {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Failed to connect to AI provider: " + apiUrl, e);
        } catch (InterruptedException e) {
            log.error("Interrupted waiting for AI response");
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI response interrupted", e);
        } catch (Exception e) {
            log.error("Error generating reply", e);
            throw new RuntimeException("Failed to generate reply: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) throws IOException {
        AIRequestPayload payload = new AIRequestPayload(
                modelName == null || modelName.isBlank() ? null : modelName,
                Objects.requireNonNullElse(prompt, ""),
                false
        );
        return objectMapper.writeValueAsString(payload);
    }

    private JsonNode callApi(String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(readTimeoutMs));

        if (contentType != null && !contentType.isBlank()) {
            requestBuilder.header("Content-Type", contentType);
        }

        if (apiKey != null && !apiKey.isBlank()) {
            if (authHeader == null || authHeader.isBlank()) {
                throw new IllegalStateException("AI auth header is missing. Set AI_AUTH_HEADER.");
            }
            String value = authPrefix == null || authPrefix.isBlank()
                    ? apiKey
                    : authPrefix.trim() + " " + apiKey;
            requestBuilder.header(authHeader, value);
        }

        HttpResponse<String> response = httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("AI provider returned status " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private String extractReply(JsonNode response) {
        if (response == null) {
            return null;
        }

        return findFirstTextValue(response);
    }

    private String findFirstTextValue(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String value = findFirstTextValue(child);
                if (value != null) {
                    return value;
                }
            }
        } else if (node.isObject()) {
            for (JsonNode child : node) {
                String value = findFirstTextValue(child);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private static final class AIRequestPayload {
        private final String model;
        private final String prompt;
        private final boolean stream;

        private AIRequestPayload(String model, String prompt, boolean stream) {
            this.model = model;
            this.prompt = prompt;
            this.stream = stream;
        }

        public String getModel() {
            return model;
        }

        public String getPrompt() {
            return prompt;
        }

        public boolean isStream() {
            return stream;
        }
    }
}
