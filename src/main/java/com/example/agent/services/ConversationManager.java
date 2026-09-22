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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationManager.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final int maxHistoryMessages;

    public ConversationManager(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        @Value("${app.chat.max-history-messages:20}") int maxHistoryMessages
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.maxHistoryMessages = maxHistoryMessages;
    }

    @Transactional
    public String saveUserMessage(ChatRequest request) {

        String conversationId = request.conversationId();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        LOGGER.debug(
            "Saving USER message | conversationId={} | messageLength={}",
            conversationId,
            request.message() != null ? request.message().length() : 0
        );

        String finalConversationId = conversationId;
        ConversationEntity conversation = conversationRepository
            .findById(conversationId)
            .orElseGet(() -> new ConversationEntity(finalConversationId, Instant.now()));

        conversation.addMessage(
            new MessageEntity(
                Role.USER,
                request.message(),
                Instant.now()
            )
        );

        conversationRepository.save(conversation);

        return conversationId;
    }

    @Transactional
    public void saveAssistantMessage(String conversationId, String content) {
        LOGGER.debug(
            "Saving ASSISTANT message | conversationId={} | messageLength={}",
            conversationId,
            content != null ? content.length() : 0
        );

        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.addMessage(
                new MessageEntity(
                    Role.ASSISTANT,
                    content,
                    Instant.now()
                )
            );

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

    @Transactional(readOnly = true)
    public List<Message> getPromptMessages(String conversationId) {

        List<Message> promptMessages = new ArrayList<>();

        promptMessages.add(
            new SystemMessage("You are a helpful, concise AI assistant.")
        );

        if (maxHistoryMessages <= 0) {
            LOGGER.debug(
                "History limit disabled | conversationId={} | maxHistoryMessages=0",
                conversationId
            );
            return promptMessages;
        }

        Pageable pageable = PageRequest.of(
            0,
            maxHistoryMessages,
            Sort.by(Sort.Direction.DESC, "id")
        );

        List<MessageEntity> history = new ArrayList<>(
            messageRepository.findByConversationId(conversationId, pageable)
        );

        Collections.reverse(history);

        LOGGER.debug(
            "Loaded conversation history for prompt | conversationId={} | maxHistoryMessages={} | loadedMessages={}",
            conversationId,
            maxHistoryMessages,
            history.size()
        );

        for (MessageEntity entity : history) {
            if (entity.getRole() == Role.USER) {
                promptMessages.add(new UserMessage(entity.getContent()));
            } else if (entity.getRole() == Role.ASSISTANT) {
                promptMessages.add(new AssistantMessage(entity.getContent()));
            }
        }

        LOGGER.debug(
            "Prompt assembled | conversationId={} | totalPromptMessages={}",
            conversationId,
            promptMessages.size()
        );

        return promptMessages;
    }

}
