package com.example.ai.demo.service;

import org.springframework.stereotype.Service;


@Service
public class ChatService {

    private final WeatherService weatherService;
    private final LlmService llmService;
    private final GoogleCalendarService calendarService;

    public ChatService(WeatherService weatherService, LlmService llmService, GoogleCalendarService calendarService) {
        this.weatherService = weatherService;
        this.llmService = llmService;
        this.calendarService = calendarService;
    }

    public String handleQuery(String query) {
        String lower = query.toLowerCase();

        if (lower.contains("weather in")) {
            // Extract city
            String city = lower.replace("what's the weather in", "")
                               .replace("whats the weather in", "")
                               .replace("today", "")
                               .trim();
            return weatherService.getWeather(city);

        } else if (lower.startsWith("book")) {
            // Book event using GoogleCalendarService with natural language parsing
            return calendarService.bookEventFromQuery(query);

        } else {
            // Any other query
            return llmService.ask(query);
        }
    }
}