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

5. **Using the Fine-tuned AEA Model** (optional)
   
   The application supports using a fine-tuned model specifically for email responses:
   
   ```bash
   # Create the fine-tuned model (run once)
   echo -e "FROM mistral\nSYSTEM \"You are an AI email assistant. Generate professional and concise email responses based on user queries. Be polite and context-aware.\"" > Modelfile
   docker exec -it ollama ollama create AEA -f Modelfile
   
   # Configure the application to use the fine-tuned model
   # Option 1: Edit application.properties
   ai.model.name=AEA
   
   # Option 2: Set environment variable
   export AI_MODEL_NAME=AEA
   
   # Option 3: Add to .env file
   AI_MODEL_NAME=AEA
   ```
   
   Note: Other users who don't have the fine-tuned model will automatically fall back to using the default Mistral model.

6. **Build and run**
   ```bash
   # Run with real email integration
   mvn clean package
   java -jar target/Email-Assistant-0.0.1-SNAPSHOT.jar
   
   # Run in test mode (no real email credentials needed)
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   ```
   
7. **Access the API**
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

4. **Fine-tuned AEA Model** (optional)
   
   For improved email responses, you can use the fine-tuned AEA model:
   
   ```bash
   # Create the fine-tuned model
   echo -e "FROM mistral\nSYSTEM \"You are an AI email assistant. Generate professional and concise email responses based on user queries. Be polite and context-aware.\"" > Modelfile
   docker exec -it ollama ollama create AEA -f Modelfile
   
   # Test the fine-tuned model
   curl -X POST http://localhost:11434/api/generate \
        -d '{"model": "AEA", "prompt": "Write a short email", "stream": false}'
   ```
   
   To use the fine-tuned model, set `ai.model.name=AEA` in your application.properties or use the environment variable `AI_MODEL_NAME=AEA`.

## Configuration Options

- **Properties File**: Use `application.properties` for configuration
- **Environment Variables**: Set values like `EMAIL_ACCOUNT_ADDRESS=your-email@gmail.com`
- **Dotenv File**: Create `.env` file based on `.env.example`
- **Test Profile**: Use `-Dspring-boot.run.profiles=test` for development without real credentials

### AI Model Configuration

The application supports configuring the AI model used for generating email responses:

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `ai.model.name` | `AI_MODEL_NAME` | `mistral` | The Ollama model to use (e.g., `mistral` or `AEA`) |
| `ai.model.api.url` | `AI_MODEL_API_URL` | `http://localhost:11434/api/generate` | The URL of the Ollama API |
| `ai.model.connect.timeout` | `AI_MODEL_CONNECT_TIMEOUT` | `10000` | Connection timeout in milliseconds |
| `ai.model.read.timeout` | `AI_MODEL_READ_TIMEOUT` | `30000` | Read timeout in milliseconds |

## Documentation

API documentation is available at `http://localhost:8081/docs` when the application is running.

## License

This project is licensed under the MIT License.
