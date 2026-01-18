package com.ai.emailassistant.infrastructure.ai;

import com.ai.emailassistant.domain.model.EmailMessage;
import com.ai.emailassistant.domain.ports.AIProvider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ollama implementation of AIProvider.
 * Handles communication with Ollama API for generating email replies.
 * Infrastructure layer only - no business logic.
 */
@Slf4j
@Component
public class OllamaAIProvider implements AIProvider {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${ai.model.name:mistral}")
    private String modelName;
    @Value("${ai.model.api-url:http://localhost:11434/api/generate}")
    private String apiUrl;
    @Value("${ai.model.temperature:0.7}")
    private Double temperature;
    @Value("${ai.model.max-tokens:500}")
    private Integer maxTokens;

    @Override
    public String generateReply(EmailMessage email, String userInstruction) {
        log.info("Generating reply using model: {} from {}", modelName, apiUrl);

        try {
            // Build prompt
            String prompt = buildPrompt(email, userInstruction);
            log.debug("Prompt ({}  chars)", prompt.length());

            // Create request
            OllamaRequest request = OllamaRequest.builder()
                    .model(modelName)
                    .prompt(prompt)
                    .stream(false)
                    .temperature(temperature)
                    .numPredict(maxTokens)
                    .build();

            // Call API
            OllamaResponse response = callApi(request);

            if (response == null || !response.isDone()) {
                log.error("Ollama did not complete generation");
                throw new RuntimeException("AI model did not complete response");
            }

            String reply = response.getResponse();
            if (reply == null || reply.isBlank()) {
                log.error("Ollama returned empty response");
                throw new RuntimeException("AI model generated empty response");
            }

            String cleaned = reply.trim();
            log.info("Generated reply ({} chars)", cleaned.length());
            return cleaned;

        } catch (IOException e) {
            log.error("IOException connecting to Ollama at {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Failed to connect to Ollama: " + apiUrl, e);
        } catch (InterruptedException e) {
            log.error("Interrupted waiting for Ollama response");
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI response interrupted", e);
        } catch (Exception e) {
            log.error("Error generating reply", e);
            throw new RuntimeException("Failed to generate reply: " + e.getMessage(), e);
        }
    }

    private OllamaResponse callApi(OllamaRequest request)
            throws IOException, InterruptedException {

        String body = objectMapper.writeValueAsString(request);
        log.debug("Calling Ollama API");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(120))
                .build();

        HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException("Ollama returned status " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), OllamaResponse.class);
    }

    private String buildPrompt(EmailMessage email, String userInstruction) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional email assistant. Generate a concise, ")
                .append("polite, and professional email reply.\n\n");

        prompt.append("=== ORIGINAL EMAIL ===\n");
        prompt.append("From: ").append(email.getFrom()).append("\n");
        prompt.append("Subject: ").append(email.getSubject()).append("\n\n");
        prompt.append("Body:\n").append(email.getSnippet()).append("\n\n");

        if (userInstruction != null && !userInstruction.isBlank()) {
            prompt.append("=== INSTRUCTIONS ===\n");
            prompt.append(userInstruction).append("\n\n");
        }

        prompt.append("=== REPLY ===\n");
        prompt.append("Generate ONLY the email body (no subject, no greeting, no closing):\n");

        return prompt.toString();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
        private Double temperature;

        @JsonProperty("num_predict")
        private Integer numPredict;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaResponse {
        private String model;
        private String response;
        private boolean done;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("total_duration")
        private Long totalDuration;

        @JsonProperty("load_duration")
        private Long loadDuration;

        @JsonProperty("prompt_eval_count")
        private Integer promptEvalCount;

        @JsonProperty("eval_count")
        private Integer evalCount;

        @JsonProperty("eval_duration")
        private Long evalDuration;
    }
}
