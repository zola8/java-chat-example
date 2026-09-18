package com.example.agent.services;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;

public interface ChatService {

    ChatResponse chat(ChatRequest request);

    ConversationResponse getConversation(String conversationId);

}
