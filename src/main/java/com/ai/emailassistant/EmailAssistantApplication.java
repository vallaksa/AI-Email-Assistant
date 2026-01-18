package com.ai.emailassistant;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Email Assistant Spring Boot Application.
 *
 * A clean architecture implementation for Gmail email fetching and AI-powered replies.
 *
 * Architecture:
 * - API Layer: REST endpoints
 * - Application Layer: Use case orchestration
 * - Domain Layer: Models and port interfaces
 * - Infrastructure Layer: Gmail and AI provider implementations
 */
@SpringBootApplication
public class EmailAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailAssistantApplication.class, args);
    }
}
