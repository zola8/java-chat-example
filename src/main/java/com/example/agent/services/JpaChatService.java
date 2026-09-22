package com.example.agent.services;


import com.example.agent.agent.AIAgent;
import com.example.agent.api.ChatStreamSink;
import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JpaChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaChatService.class);

    private final ConversationManager conversationManager;
    private final AIAgent aiAgent;

    public JpaChatService(
        ConversationManager conversationManager,
        AIAgent aiAgent
    ) {
        this.conversationManager = conversationManager;
        this.aiAgent = aiAgent;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 1. Save user message
        String conversationId = conversationManager.saveUserMessage(request);

        // 2. Load full history (System Prompt + History + Current User Message)
        List<Message> promptMessages = conversationManager.getPromptMessages(conversationId);

        // 3. Call the Agent (Tools happen automatically inside!)
        String reply;
        try {
            LOGGER.debug("Calling AIAgent synchronously | conversationId={}, Q: {}", conversationId, request.message());
            reply = aiAgent.generate(promptMessages);
        } catch (Exception exception) {
            LOGGER.error("AI call failed", exception);
            throw new IllegalStateException("AI call failed", exception);
        }

        if (reply == null) reply = "";

        // 4. Save assistant message
        conversationManager.saveAssistantMessage(conversationId, reply);

        return new ChatResponse(conversationId, reply, Instant.now());
    }

    @Override
    public ConversationResponse getConversation(String conversationId) {
        return conversationManager.getConversation(conversationId);
    }


    @Override
    public void streamChat(ChatRequest request, ChatStreamSink sink) {
        Thread.ofVirtual().name("chat-stream-orchestrator").start(() -> {
            try {
                // 1. Save user message
                String conversationId = conversationManager.saveUserMessage(request);

                // 2. Load full history
                List<Message> promptMessages = conversationManager.getPromptMessages(conversationId);

                StringBuilder fullMessage = new StringBuilder();

                // 3. Stream from the Agent
                aiAgent.stream(promptMessages).subscribe(
                    chatResponse -> {
                        if (!sink.isActive()) return;

                        if (chatResponse != null
                            && chatResponse.getResult() != null
                            && chatResponse.getResult().getOutput() != null) {

                            String textChunk = chatResponse.getResult().getOutput().getText();
                            if (textChunk != null && !textChunk.isEmpty()) {
                                sink.sendToken(textChunk);
                                fullMessage.append(textChunk);
                            }
                        }
                    },
                    error -> {
                        if (sink.isActive()) {
                            sink.sendError("STREAM_ERROR", error.getMessage());
                            sink.close();
                        }
                    },
                    () -> {
                        if (sink.isActive()) {
                            conversationManager.saveAssistantMessage(
                                conversationId,
                                fullMessage.toString()
                            );
                            sink.sendDone(conversationId, fullMessage.toString());
                            sink.close();
                        }
                    }
                );

            } catch (Exception e) {
                sink.sendError("ORCHESTRATION_ERROR", e.getMessage());
                sink.close();
            }
        });
    }

}
