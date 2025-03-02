package com.AI_Powered.Email.Assistant.Config;

import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailFetcher;
import com.AI_Powered.Email.Assistant.AIEmailAssitService.EmailSenderService;
import com.AI_Powered.Email.Assistant.AIEmailAssitService.MockEmailFetcher;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException;
import com.AI_Powered.Email.Assistant.exception.EmailAssistantException.ErrorCode;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Date;
import java.util.Properties;

/**
 * Test configuration that provides mock email services
 * to allow the application to start without real email credentials.
 * This is activated by the 'test' Spring profile.
 */
@Configuration
@Profile("test")
public class TestConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    
    @Bean
    @Primary
    public JavaMailSender mockMailSender() {
        logger.info("Creating mock JavaMailSender for test profile");
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setUsername("test@example.com");
        mailSender.setPassword("password");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.trust", "localhost");
        
        logger.info("Mock JavaMailSender configured successfully");
        return mailSender;
    }
    
    @Bean
    @Primary
    public EmailSenderService mockEmailSenderService(JavaMailSender mailSender) {
        logger.info("Creating mock EmailSenderService for test profile");
        
        return new EmailSenderService(mailSender) {
            @Override
            public void replyToEmail(jakarta.mail.Message email) {
                logger.info("MOCK: Replying to email with mock implementation");
                // In a real implementation, this would send an actual email
            }
            
            @Override
            public void sendEmail(String to, String subject, String body) {
                logger.info("MOCK: Sending email to {} with subject: {}", to, subject);
                // In a real implementation, this would send an actual email
            }
        };
    }
    
    @Bean
    @Primary
    public EmailFetcher mockEmailFetcher(EmailProperties emailProperties, Environment environment, JavaMailSender mailSender) {
        logger.info("Creating mock EmailFetcher for test profile");
        // Initialize the real EmailFetcher with the test environment
        return new EmailFetcher(emailProperties, environment, mailSender);
    }
    
    @Bean
    @Primary
    public MockEmailFetcher mockEmailFetcherService(JavaMailSender mailSender) {
        logger.info("Creating MockEmailFetcher service for test profile");
        return new MockEmailFetcher(mailSender);
    }
} 