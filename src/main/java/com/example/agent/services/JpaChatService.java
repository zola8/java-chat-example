package com.example.agent.services;


import com.example.agent.api.ChatStreamSink;
import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;
import com.example.agent.persistence.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class JpaChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaChatService.class);

    private final ConversationManager conversationManager;
    private final StreamingChatService streamingGenerator;
    private final ConversationRepository conversationRepository;

    public JpaChatService(
        ConversationManager conversationManager,
        StreamingChatService streamingGenerator,
        ConversationRepository conversationRepository
    ) {
        this.conversationManager = conversationManager;
        this.streamingGenerator = streamingGenerator;
        this.conversationRepository = conversationRepository;
    }


    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String conversationId = conversationManager.saveUserMessage(request);
        String reply = "Echo (sync): " + request.message();

        conversationManager.saveAssistantMessage(conversationId, reply);

        return new ChatResponse(conversationId, reply, Instant.now());
    }

    @Override
    public ConversationResponse getConversation(String conversationId) {
        return conversationManager.getConversation(conversationId);
    }


    @Override
    public void streamChat(ChatRequest request, ChatStreamSink sink) {
        // Run the entire orchestration flow on a virtual thread
        Thread.ofVirtual().name("chat-stream-orchestrator").start(() -> {
            try {
                // 1. Save User Message (Short DB transaction)
                String conversationId = conversationManager.saveUserMessage(request);

                // 2. Accumulate tokens and stream to client
                StringBuilder fullMessage = new StringBuilder();

                streamingGenerator.stream(request, sink::isActive, new StreamListener() {
                    @Override
                    public void onToken(String token) {
                        if (sink.isActive()) {
                            sink.sendToken(token);
                            fullMessage.append(token);
                        }
                    }

                    @Override
                    public void onComplete(String cId, String msg) {
                        if (sink.isActive()) {
                            // 3. Save Assistant Message (Short DB transaction)
                            conversationManager.saveAssistantMessage(conversationId, fullMessage.toString());

                            // 4. Send Done event and close
                            sink.sendDone(conversationId, fullMessage.toString());
                            sink.close();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (sink.isActive()) {
                            sink.sendError("STREAM_ERROR", error.getMessage());
                            sink.close();
                        }
                    }
                });

            } catch (Exception e) {
                sink.sendError("ORCHESTRATION_ERROR", e.getMessage());
                sink.close();
            }
        });
    }

}
