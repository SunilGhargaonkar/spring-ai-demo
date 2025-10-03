package com.example.ai.demo.controller;

import com.example.ai.demo.dto.ChatRequest;
import com.example.ai.demo.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {

        return chatService.handleQuery(request.query());
    }
}