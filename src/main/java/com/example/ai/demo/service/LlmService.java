package com.example.ai.demo.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LlmService {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${openrouter.api.url}")
    private String openRouterUrl;
    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.model}")
    private String model;

    public String ask(String query) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, Object> requestBody = Map.of("model", model, "messages",
                List.of(Map.of("role", "user", "content", query)));
        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        final ResponseEntity<String> response =
                restTemplate.exchange(openRouterUrl, HttpMethod.POST, entity, String.class);
        final JSONObject json = new JSONObject(Objects.requireNonNull(response.getBody()));

        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }
}