package com.ai.emailassistant.infrastructure.ai.http;

import com.ai.emailassistant.common.ApiConstants;
import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.port.AIProvider;
import com.ai.emailassistant.exception.ProviderException;
import com.ai.emailassistant.infrastructure.ai.PromptBuilder;
import com.ai.emailassistant.infrastructure.ai.config.AIProviderProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Generic HTTP-based AI provider driven entirely by configuration.
 * Bean creation is managed by AIProviderFactory.
 */
@Slf4j
public class HttpAIProvider implements AIProvider {

    private final AIProviderProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public HttpAIProvider(AIProviderProperties properties) {
        this.properties = properties;
        // Initialize HTTP client with connection pooling for better performance
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeouts().getConnectMs()))
                // Enable HTTP/2 and connection reuse
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    public String generateReply(EmailMessage email, String userInstruction) {
        String apiUrl = properties.getApi().getUrl();
        log.info("Generating reply using configured AI provider at {}", apiUrl);

        try {
            String prompt = PromptBuilder.buildPrompt(email, userInstruction);
            String body = buildRequestBody(prompt);
            JsonNode response = callApi(body);
            String reply = extractReply(response);

            if (reply == null || reply.isBlank()) {
                throw new ProviderException("AI provider returned empty response");
            }

            String cleaned = reply.trim();
            log.info("Generated reply ({} chars)", cleaned.length());
            return cleaned;
        } catch (IOException e) {
            log.error("IOException connecting to AI provider at {}: {}", apiUrl, e.getMessage());
            throw new ProviderException("Failed to connect to AI provider: " + apiUrl, e);
        } catch (InterruptedException e) {
            log.error("Interrupted waiting for AI response");
            Thread.currentThread().interrupt();
            throw new ProviderException("AI response interrupted", e);
        } catch (Exception e) {
            log.error("Error generating reply", e);
            throw new ProviderException("Failed to generate reply: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) throws IOException {
        AIRequestPayload payload = new AIRequestPayload(
                properties.getModel().getName() == null || properties.getModel().getName().isBlank() 
                    ? null 
                    : properties.getModel().getName(),
                Objects.requireNonNullElse(prompt, ""),
                false
        );
        return objectMapper.writeValueAsString(payload);
    }

    private JsonNode callApi(String body) throws IOException, InterruptedException {
        String apiUrl = properties.getApi().getUrl();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(properties.getTimeouts().getReadMs()));

        String contentType = properties.getApi().getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = ApiConstants.DEFAULT_CONTENT_TYPE;
        }
        requestBuilder.header("Content-Type", contentType);

        String apiKey = properties.getApi().getKey();
        if (apiKey != null && !apiKey.isBlank()) {
            String authHeader = properties.getApi().getAuthHeader();
            if (authHeader == null || authHeader.isBlank()) {
                throw new IllegalStateException("AI auth header is missing. Set AI_AUTH_HEADER.");
            }
            String authPrefix = properties.getApi().getAuthPrefix();
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

        // Log the response structure for debugging
        log.debug("AI API response structure: {}", response.toPrettyString());

        // Try to find reply in common response field names first
        String reply = findReplyInCommonFields(response);
        if (reply != null && !reply.isBlank()) {
            return reply;
        }

        // Fallback to searching for any text value, but skip known non-reply fields
        return findReplyTextValue(response);
    }

    /**
     * Looks for reply in common response field names.
     */
    private String findReplyInCommonFields(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        // Common field names that typically contain the reply
        String[] replyFields = {"response", "content", "text", "message", "reply", "output", "answer", "generated_text"};
        
        for (String field : replyFields) {
            JsonNode fieldNode = node.get(field);
            if (fieldNode != null) {
                if (fieldNode.isTextual()) {
                    return fieldNode.asText();
                }
                // Handle nested structures like {"message": {"content": "..."}}
                if (fieldNode.isObject()) {
                    String nested = findReplyInCommonFields(fieldNode);
                    if (nested != null) {
                        return nested;
                    }
                }
                // Handle arrays like {"choices": [{"message": {"content": "..."}}]}
                if (fieldNode.isArray() && fieldNode.size() > 0) {
                    JsonNode first = fieldNode.get(0);
                    if (first.isObject()) {
                        String nested = findReplyInCommonFields(first);
                        if (nested != null) {
                            return nested;
                        }
                    } else if (first.isTextual()) {
                        return first.asText();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Recursively searches for text values, but skips known non-reply fields.
     */
    private String findReplyTextValue(JsonNode node) {
        if (node == null) {
            return null;
        }

        // Fields to skip (these are not the reply)
        String[] skipFields = {"model", "prompt", "stream", "temperature", "max_tokens", "top_p", "frequency_penalty", "presence_penalty"};

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                String value = findReplyTextValue(child);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        } else if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode child = entry.getValue();
                
                // Skip known non-reply fields
                boolean shouldSkip = false;
                for (String skipField : skipFields) {
                    if (skipField.equalsIgnoreCase(fieldName)) {
                        shouldSkip = true;
                        break;
                    }
                }
                
                if (!shouldSkip) {
                    String value = findReplyTextValue(child);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
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
