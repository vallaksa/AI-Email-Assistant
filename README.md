# Email Assistant API

A Spring Boot application that provides a RESTful API for managing emails using Gmail with OAuth2 authentication and AI-powered responses.

## Features

- **Email Management**: Fetch emails from your inbox and send AI-generated replies
- **Gmail OAuth2 Integration**: Secure access to your Gmail account
- **AI-Powered Responses**: Generate intelligent email replies using a configurable AI provider
- **Test Mode**: Run with mock implementations for development without real credentials
- **Flexible Configuration**: Environment variables or properties file for easy setup
- **Security-Focused**: Sensitive data kept out of source control

## Technologies

- Java 17+ and Spring Boot 3.4.3
- Gmail API with OAuth2 authentication
- Configurable AI providers (local or hosted)

## Quick Start

### Prerequisites

- JDK 17 or higher
- Maven 3.6+
- Docker (optional, for running a local AI server)
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

4. **Configure AI provider** (optional for test mode)
   - Local provider (default): set `AI_PROVIDER=local` and point `AI_LOCAL_API_URL` to your local server.
   - Hosted provider: set `AI_PROVIDER=openai` and provide `AI_OPENAI_API_KEY`.

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
POST /api/emails/fetch
```

Request body example:
```json
{
  "limit": 10
}
```

### Reply to Emails
```
POST /api/emails/reply
```

Request body example:
```json
{
  "index": 1,
  "userInstruction": "Reply with a polite thank-you and ask for next steps."
}
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

### AI Provider Setup

Provide an AI base URL via environment variables. The app uses a fixed request payload and parses a plain-text reply.

## Configuration Options

- **Properties File**: Use `application.properties` for configuration
- **Environment Variables**: Set values like `EMAIL_ACCOUNT_ADDRESS=your-email@gmail.com`
- **Dotenv File**: Create `.env` file based on `.env.example`
- **Test Profile**: Use `-Dspring-boot.run.profiles=test` for development without real credentials

### AI Model Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `ai.api.url` | `AI_API_URL` | *(required)* | Base URL for the AI HTTP endpoint |
| `ai.api.key` | `AI_API_KEY` | *(optional)* | API key (enables auth header) |
| `ai.api.auth-header` | `AI_AUTH_HEADER` | *(optional)* | Header name for auth (required if key is set) |
| `ai.api.auth-prefix` | `AI_AUTH_PREFIX` | *(optional)* | Prefix for auth header (e.g., `Bearer`) |
| `ai.api.content-type` | `AI_CONTENT_TYPE` | `application/json` | Content type for the request |
| `ai.model.name` | `AI_MODEL_NAME` | *(optional)* | Model id included in the request payload |

## Documentation

API documentation is available at `http://localhost:8081/docs` when the application is running.

## License

This project is licensed under the MIT License.
