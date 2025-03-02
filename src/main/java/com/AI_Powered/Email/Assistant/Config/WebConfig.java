package com.AI_Powered.Email.Assistant.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect to the API documentation
        registry.addRedirectViewController("/docs", "/docs/index.html");
        registry.addRedirectViewController("/api-docs", "/docs/index.html");
    }
} 