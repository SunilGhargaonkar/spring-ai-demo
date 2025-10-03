package com.example.ai.demo.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${openweather.api.url}")
    private String weatherApiUrl;
    @Value("${openweather.api.key}")
    private String weatherApiKey;

    public String getWeather(String city) {
        final String url = UriComponentsBuilder.fromHttpUrl(weatherApiUrl)
                .queryParam("q", city)
                .queryParam("appid", weatherApiKey)
                .queryParam("units", "metric")
                .toUriString();
        final String response = restTemplate.getForObject(url, String.class);
        final JSONObject json = new JSONObject(response);

        if (json.has("main")) {
            double temp = json.getJSONObject("main").getDouble("temp");
            return String.format("The current temperature in %s is %.1f °C", city, temp);
        } else {
            return "I couldn’t fetch the weather right now.";
        }
    }
}