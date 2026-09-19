package com.example.agent.services;

import com.example.agent.api.dto.ChatRequest;

import java.util.function.BooleanSupplier;


public interface StreamingChatService {

    void stream(
        ChatRequest request,
        BooleanSupplier active,
        StreamListener listener
    );
}
