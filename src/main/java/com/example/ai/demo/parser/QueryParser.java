package com.example.ai.demo.parser;

import org.springframework.stereotype.Component;

@Component
public class QueryParser {
    public String extractTitle(String query) {
        return query.contains("for") 
                ? query.substring(query.indexOf("for") + 4).trim() 
                : "Event";
    }
}
