package com.example.agent.services;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;
import com.example.agent.api.dto.MessageResponse;
import com.example.agent.domain.Role;
import com.example.agent.exception.ConversationNotFoundException;
import com.example.agent.persistence.entity.ConversationEntity;
import com.example.agent.persistence.entity.MessageEntity;
import com.example.agent.persistence.repository.ConversationRepository;
import com.example.agent.persistence.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JpaChatService implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public JpaChatService(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {

        String conversationId = request.conversationId();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String finalConversationId = conversationId;
        ConversationEntity conversation = conversationRepository
            .findById(conversationId)
            .orElseGet(() -> new ConversationEntity(finalConversationId, Instant.now()));

        Instant userMessageTime = Instant.now();

        conversation.addMessage(
            new MessageEntity(
                Role.USER,
                request.message(),
                userMessageTime
            )
        );

        String reply = "Echo: " + request.message();

        Instant assistantMessageTime = Instant.now();

        conversation.addMessage(
            new MessageEntity(
                Role.ASSISTANT,
                reply,
                assistantMessageTime
            )
        );

        conversationRepository.save(conversation);

        return new ChatResponse(
            conversationId,
            reply,
            assistantMessageTime
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(String conversationId) {

        ConversationEntity conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        List<MessageResponse> messages = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId)
            .stream()
            .map(message -> new MessageResponse(
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
            ))
            .toList();

        return new ConversationResponse(
            conversation.getId(),
            conversation.getCreatedAt(),
            messages
        );
    }
}
