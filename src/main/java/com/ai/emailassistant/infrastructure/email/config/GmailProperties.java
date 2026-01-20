package com.ai.emailassistant.infrastructure.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for Gmail integration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "gmail")
public class GmailProperties {
    
    private Account account = new Account();
    private OAuth2 oauth2 = new OAuth2();
    private Imap imap = new Imap();
    private Smtp smtp = new Smtp();
    
    @Data
    public static class Account {
        private String email;
    }
    
    @Data
    public static class OAuth2 {
        private String clientId;
        private String clientSecret;
        private List<String> scopes;
    }
    
    @Data
    public static class Imap {
        private String host;
        private Integer port;
        private String username;
        private String authMechanism;
        private Boolean ssl;
    }
    
    @Data
    public static class Smtp {
        private String host;
        private Integer port;
        private String username;
        private String authMechanism;
        private Boolean starttls;
        private String sslTrust;
    }
}
