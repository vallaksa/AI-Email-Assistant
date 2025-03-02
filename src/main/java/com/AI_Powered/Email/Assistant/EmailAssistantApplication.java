package com.AI_Powered.Email.Assistant;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;

@SpringBootApplication
@EnableConfigurationProperties
public class EmailAssistantApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailAssistantApplication.class);

	public static void main(String[] args) {
		// Load environment variables from .env file if it exists
		File envFile = new File(".env");
		if (envFile.exists()) {
			try {
				logger.info("Loading environment variables from .env file");
				Dotenv dotenv = Dotenv.configure().load();
				
				// Set environment variables from .env file
				dotenv.entries().forEach(entry -> {
					if (System.getenv(entry.getKey()) == null) {
						System.setProperty(entry.getKey(), entry.getValue());
					}
				});
			} catch (Exception e) {
				logger.warn("Failed to load .env file: {}", e.getMessage());
			}
		}
		
		SpringApplication.run(EmailAssistantApplication.class, args);
	}

}
