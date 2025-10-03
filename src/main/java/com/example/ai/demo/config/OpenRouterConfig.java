package com.example.ai.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }
}