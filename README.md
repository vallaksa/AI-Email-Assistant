# Email Assistant API

A Spring Boot application that provides a RESTful API for managing emails using Gmail with OAuth2 authentication and AI-powered responses.

## Features

- **Email Management**: Fetch emails from your inbox and send AI-generated replies
- **Gmail OAuth2 Integration**: Secure access to your Gmail account
- **AI-Powered Responses**: Generate intelligent email replies using Ollama with Mistral LLM
- **Test Mode**: Run with mock implementations for development without real credentials
- **Flexible Configuration**: Environment variables or properties file for easy setup
- **Security-Focused**: Sensitive data kept out of source control

## Technologies

- Java 17+ and Spring Boot 3.4.3
- Gmail API with OAuth2 authentication
- Ollama with Mistral LLM for AI responses
- Docker for running the AI model

## Quick Start

### Prerequisites

- JDK 17 or higher
- Maven 3.6+
- Docker (for running Ollama)
- Google API credentials (not required for test mode)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/Email-Assistant.git
   cd Email-Assistant
   ```

2. **Setup Google API credentials** (skip if using test mode)
   - Create a project in [Google Cloud Console](https://console.cloud.google.com/)
   - Enable the Gmail API and set up OAuth consent screen
   - Create OAuth credentials and download as `credentials.json`
   - Place `credentials.json` in the project root directory

3. **Configure your email**
   ```bash
   cp src/main/resources/application.properties.template src/main/resources/application.properties
   ```
   Edit to set your Gmail address:
   ```properties
   email.account.address=your-email@gmail.com
   ```

4. **Setup Ollama with Mistral** (optional for test mode)
   ```bash
   # Pull and run Ollama
   docker pull ghcr.io/ollama/ollama
   docker run --rm -d --name ollama -p 11434:11434 ghcr.io/ollama/ollama
   
   # Pull the Mistral model
   docker exec -it ollama ollama pull mistral
   ```

5. **Build and run**
   ```bash
   # Run with real email integration
   mvn clean package
   java -jar target/Email-Assistant-0.0.1-SNAPSHOT.jar
   
   # Run in test mode (no real email credentials needed)
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   ```
   
6. **Access the API**
   ```
   http://localhost:8081
   ```

## Test Mode

The application supports a `test` profile that uses mock implementations instead of connecting to real email servers:

### Features in Test Mode

- **No External Dependencies**: Works without Gmail API credentials
- **Mock Emails**: Returns simulated email messages
- **Mock AI Responses**: Provides predefined responses instead of calling the LLM
- **Simplified Testing**: Focus on API functionality without external services

### Running in Test Mode

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Using Java jar
java -jar target/Email-Assistant-0.0.1-SNAPSHOT.jar --spring.profiles.active=test

# Using .env file
# Add SPRING_PROFILES_ACTIVE=test to your .env file
```

## API Endpoints

### Fetch Emails
```
GET /email/fetch/{count}
GET /email/fetch?count={number}
```

### Reply to Emails
```
POST /email/reply/{emailIndex}
POST /email/reply?emailIndex={index}
```

## Detailed Setup Guide

### Gmail API Credentials

1. **Create a Google Cloud Project**
   - Visit [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project

2. **Enable Gmail API**
   - Go to "APIs & Services" > "Library"
   - Search for and enable "Gmail API"

3. **Configure OAuth**
   - Set up the OAuth consent screen (External)
   - Add scope: `https://mail.google.com/`
   - Add your email as a test user

4. **Create Credentials**
   - Create OAuth client ID (Desktop app type)
   - Download the JSON file and rename to `credentials.json`

### Ollama Mistral Setup

The application uses Ollama with Mistral LLM for generating email replies:

1. **Run Ollama**
   ```bash
   docker run --rm -d --name ollama -p 11434:11434 ghcr.io/ollama/ollama
   ```

2. **Install Mistral**
   ```bash
   docker exec -it ollama ollama pull mistral
   ```

3. **Verify Installation**
   ```bash
   curl -X POST http://localhost:11434/api/generate \
        -d '{"model": "mistral", "prompt": "Write a short email", "stream": false}'
   ```

## Configuration Options

- **Properties File**: Use `application.properties` for configuration
- **Environment Variables**: Set values like `EMAIL_ACCOUNT_ADDRESS=your-email@gmail.com`
- **Dotenv File**: Create `.env` file based on `.env.example`
- **Test Profile**: Use `-Dspring-boot.run.profiles=test` for development without real credentials

## Documentation

API documentation is available at `http://localhost:8081/docs` when the application is running.

## License

This project is licensed under the MIT License.
