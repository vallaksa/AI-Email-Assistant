package com.ai.emailassistant.infrastructure.ai;

import com.ai.emailassistant.domain.port.AIProvider;
import com.ai.emailassistant.infrastructure.ai.config.AIProviderProperties;
import com.ai.emailassistant.infrastructure.ai.http.HttpAIProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory for creating AI provider beans based on configuration.
 * Uses conditional bean creation to select the appropriate provider.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AIProviderFactory {

    private final AIProviderProperties properties;

    /**
     * Creates HttpAIProvider bean when ai.provider.type=http or not specified.
     */
    @Bean
    @ConditionalOnProperty(name = "ai.provider.type", havingValue = "http", matchIfMissing = true)
    public AIProvider httpAIProvider() {
        log.info("Creating HttpAIProvider bean");
        return new HttpAIProvider(properties);
    }

    // Future providers can be added here:
    // @Bean
    // @ConditionalOnProperty(name = "ai.provider.type", havingValue = "openai")
    // public AIProvider openAIProvider(OpenAIProperties properties) {
    //     return new OpenAIAIProvider(properties);
    // }
}
