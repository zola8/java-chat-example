package com.example.agent.services;

import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.function.BooleanSupplier;


public interface StreamingChatService {

    void stream(
        List<Message> messages,
        BooleanSupplier active,
        StreamListener listener
    );
}
