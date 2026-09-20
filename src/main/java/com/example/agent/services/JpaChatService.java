package com.example.agent.services;


import com.example.agent.api.ChatStreamSink;
import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JpaChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaChatService.class);

    private final ConversationManager conversationManager;
    private final StreamingChatService streamingGenerator;
    private final ChatClient chatClient;

    public JpaChatService(
        ConversationManager conversationManager,
        StreamingChatService streamingGenerator,
        ChatModel chatModel
    ) {
        this.conversationManager = conversationManager;
        this.streamingGenerator = streamingGenerator;
        this.chatClient = ChatClient.builder(chatModel).build();
    }


    @Override
    public ChatResponse chat(ChatRequest request) {
        String conversationId = conversationManager.saveUserMessage(request);
        String reply;

        try {
            reply = chatClient
                .prompt()
                .user(request.message())
                .call()
                .content();

        } catch (Exception exception) {
            throw new IllegalStateException("AI call failed", exception);
        }

        if (reply == null) {
            reply = "";
        }

        conversationManager.saveAssistantMessage(conversationId, reply);

        return new ChatResponse(
            conversationId,
            reply,
            Instant.now()
        );
    }

    @Override
    public ConversationResponse getConversation(String conversationId) {
        return conversationManager.getConversation(conversationId);
    }


    @Override
    public void streamChat(ChatRequest request, ChatStreamSink sink) {
        Thread.ofVirtual().name("chat-stream-orchestrator").start(() -> {
            try {
                String conversationId = conversationManager.saveUserMessage(request);

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
                            conversationManager.saveAssistantMessage(conversationId, fullMessage.toString());
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
