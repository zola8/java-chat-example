package com.example.agent.services;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ConversationResponse;
import com.example.agent.api.dto.MessageResponse;
import com.example.agent.domain.Role;
import com.example.agent.exception.ConversationNotFoundException;
import com.example.agent.persistence.entity.ConversationEntity;
import com.example.agent.persistence.entity.MessageEntity;
import com.example.agent.persistence.repository.ConversationRepository;
import com.example.agent.persistence.repository.MessageRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationManager {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationManager(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public String saveUserMessage(ChatRequest request) {
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String finalConversationId = conversationId;
        ConversationEntity conversation = conversationRepository
            .findById(conversationId)
            .orElseGet(() -> new ConversationEntity(finalConversationId, Instant.now()));

        conversation.addMessage(new MessageEntity(Role.USER, request.message(), Instant.now()));
        conversationRepository.save(conversation);

        return conversationId;
    }

    @Transactional
    public void saveAssistantMessage(String conversationId, String content) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.addMessage(new MessageEntity(Role.ASSISTANT, content, Instant.now()));
            conversationRepository.save(conversation);
        });
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(String conversationId) {
        ConversationEntity conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        List<MessageResponse> messages = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId)
            .stream()
            .map(m -> new MessageResponse(m.getRole(), m.getContent(), m.getCreatedAt()))
            .toList();

        return new ConversationResponse(conversation.getId(), conversation.getCreatedAt(), messages);
    }

    /**
     * Loads conversation history and formats it for the LLM prompt.
     */
    @Transactional(readOnly = true)
    public List<Message> getPromptMessages(String conversationId) {
        List<Message> promptMessages = new ArrayList<>();

        // 1. Add System Prompt
        promptMessages.add(new SystemMessage("You are a helpful, concise AI assistant. Answer briefly."));

        // 2. Load history from DB
        List<MessageEntity> history = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId);

        // 3. Map DB entities to Spring AI Messages
        for (MessageEntity entity : history) {
            if (entity.getRole() == Role.USER) {
                promptMessages.add(new UserMessage(entity.getContent()));
            } else if (entity.getRole() == Role.ASSISTANT) {
                promptMessages.add(new AssistantMessage(entity.getContent()));
            }
        }

        return promptMessages;
    }

}
