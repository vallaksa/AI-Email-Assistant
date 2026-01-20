package com.ai.emailassistant.infrastructure.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for AI provider.
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIProviderProperties {
    
    private Provider provider = new Provider();
    private Api api = new Api();
    private Model model = new Model();
    private Timeouts timeouts = new Timeouts();
    
    @Data
    public static class Provider {
        private String type = "http";
    }
    
    @Data
    public static class Api {
        private String url;
        private String key;
        private String authHeader;
        private String authPrefix;
        private String contentType = "application/json";
    }
    
    @Data
    public static class Model {
        private String name;
    }
    
    @Data
    public static class Timeouts {
        private Integer connectMs = 10000;
        private Integer readMs = 60000;
    }
}
