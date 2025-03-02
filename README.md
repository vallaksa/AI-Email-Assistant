=======
# Email Assistant API

A Spring Boot application that provides a RESTful API for managing emails, including fetching and replying to emails from your Gmail account using OAuth2 authentication.

## 📋 Features

- **Email Fetching**: Retrieve emails from your Gmail inbox
- **Email Replying**: Send replies to received emails
- **OAuth2 Authentication**: Secure Gmail API integration
- **RESTful API**: Clean API design with both query parameter and path variable support
- **Documentation**: Comprehensive API documentation using Spring REST Docs and AsciiDoc

## 🛠️ Technologies

- Java 17+
- Spring Boot 3.4.3
- OAuth2 for Gmail API authentication
- Jakarta Mail API for email operations
- Maven for dependency management and build
- AsciiDoc for API documentation

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- Google API credentials for Gmail access

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Email-Assistant.git
   cd Email-Assistant
   ```

2. Configure Gmail OAuth2 credentials:
   - Place your `credentials.json` file in the root directory
   - The file should contain your OAuth 2.0 Client ID and Client Secret from Google Cloud Console

3. Build the application:
   ```bash
   mvn clean package
   ```

4. Run the application:
   ```bash
   java -jar target/Email-Assistant-0.0.1-SNAPSHOT.jar
   ```

5. The API will be available at:
   ```
   http://localhost:8081
   ```

## ⚙️ Configuration

### Application Properties

Key application properties are configured in `src/main/resources/application.properties`:

- Server port: 8081
- Logging configurations
- Email service configurations

### OAuth2 Setup

On first run, the application will prompt you to authorize access to your Gmail account:
1. A browser will open with a Google authorization page
2. Sign in with your Google account and grant the requested permissions
3. The authorization token will be saved for future use

## 📚 API Documentation

The API documentation is available at:
```
http://localhost:8081/docs
```

### API Endpoints

#### Fetch Emails

- Using Query Parameters:
  ```
  GET /email/fetch?count={number}
  ```

- Using Path Variables:
  ```
  GET /email/fetch/{count}
  ```
  
#### Reply to Emails

- Using Query Parameters:
  ```
  POST /email/reply?emailIndex={index}
  ```

- Using Path Variables:
  ```
  POST /email/reply/{emailIndex}
  ```

## 📝 Usage Examples

### Fetch Emails

```bash
# Fetch 5 emails (using query parameters)
curl -X GET "http://localhost:8081/email/fetch?count=5" -H "accept: application/json"

# Fetch 3 emails (using path variables)
curl -X GET "http://localhost:8081/email/fetch/3" -H "accept: application/json"
```

### Reply to an Email

```bash
# Reply to the most recent email (using query parameters)
curl -X POST "http://localhost:8081/email/reply?emailIndex=1" \
     -H "accept: application/json" \
     -H "Content-Type: application/json" \
     -d '{"replyText": "Thank you for your email. I will get back to you soon."}'

# Reply to the most recent email (using path variables)
curl -X POST "http://localhost:8081/email/reply/1" \
     -H "accept: application/json" \
     -H "Content-Type: application/json" \
     -d '{"replyText": "Thank you for your email. I will get back to you soon."}'
```

## 🧪 Testing

Run the tests using Maven:

```bash
mvn test
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Google API Client Library for Java](https://github.com/googleapis/google-api-java-client)
- [Jakarta Mail](https://eclipse-ee4j.github.io/mail/) 
>>>>>>> b191683 (Initial commit)
