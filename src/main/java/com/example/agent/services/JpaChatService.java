package com.example.agent.services;


import com.example.agent.agent.AIAgent;
import com.example.agent.api.ChatStreamSink;
import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ConversationResponse;
import com.example.agent.api.dto.rag.SearchResult;
import com.example.agent.services.rag.DocumentRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JpaChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaChatService.class);

    private final ConversationManager conversationManager;
    private final AIAgent aiAgent;
    private final DocumentRetrievalService retrievalService;

    public JpaChatService(
        ConversationManager conversationManager,
        AIAgent aiAgent,
        DocumentRetrievalService retrievalService
    ) {
        this.conversationManager = conversationManager;
        this.aiAgent = aiAgent;
        this.retrievalService = retrievalService;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 1. Save user message
        String conversationId = conversationManager.saveUserMessage(request);

        // 2. Load full history (System Prompt + History + Current User Message)
        List<Message> promptMessages = conversationManager.getPromptMessages(conversationId);

        // --- RAG AUGMENTATION START ---
        augmentPromptWithRag(promptMessages, request.message());
        // --- RAG AUGMENTATION END ---

        // 3. Call the Agent (Tools happen automatically inside!)
        String reply;
        try {
            LOGGER.debug("Q: {}", request.message());
            reply = aiAgent.generate(promptMessages);
            LOGGER.debug("A: {}", reply);
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

                // --- RAG AUGMENTATION START ---
                augmentPromptWithRag(promptMessages, request.message());
                // --- RAG AUGMENTATION END ---

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


    /**
     * Searches the vector store and injects relevant context into the prompt.
     */
    private void augmentPromptWithRag(List<Message> promptMessages, String userQuery) {
        try {
            LOGGER.debug("Searching RAG context for query: {}", userQuery);

            // Retrieve top 3 relevant chunks
            List<SearchResult> contextResults = retrievalService.findRelevantContext(userQuery, 3);

            if (!contextResults.isEmpty()) {
                LOGGER.debug("Found {} RAG chunks. Augmenting prompt.", contextResults.size());

                String contextBlock = formatContext(contextResults);

                String ragInstruction = """
                    You have access to the following context information from a knowledge base.
                    Use it to answer the user's question if it is relevant.
                    If the answer is not in the context, you can rely on your general knowledge, but prefer the context when applicable.
                    
                    CONTEXT:
                    %s
                    """.formatted(contextBlock);

                // Insert the RAG System Message right before the last User Message
                int insertIndex = Math.max(0, promptMessages.size() - 1);
                promptMessages.add(insertIndex, new SystemMessage(ragInstruction));
            } else {
                LOGGER.debug("No relevant RAG context found for query.");
            }
        } catch (Exception e) {
            LOGGER.warn("RAG retrieval failed, falling back to standard prompt", e);
            // We don't throw here. If RAG fails, the AI should still try to answer from memory/training.
        }
    }

    private String formatContext(List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append("[Source ").append(i + 1).append("]\n")
                .append(results.get(i).content())
                .append("\n\n");
        }
        return sb.toString();
    }
}
