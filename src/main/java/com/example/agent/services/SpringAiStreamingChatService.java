package com.example.agent.services;


import com.example.agent.api.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.BooleanSupplier;

@Service
public class SpringAiStreamingChatService implements StreamingChatService {

    private final ChatClient chatClient;

    public SpringAiStreamingChatService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public void stream(ChatRequest request, BooleanSupplier active, StreamListener listener) {

        Flux<ChatResponse> flux = chatClient.prompt()
            .user(request.message())
            .stream()
            .chatResponse();

        flux.subscribe(
            chatResponse -> {
                if (!active.getAsBoolean()) {
                    return;
                }

                // Extract the text chunk
                if (chatResponse != null
                    && chatResponse.getResult() != null
                    && chatResponse.getResult().getOutput() != null) {

                    String textChunk = chatResponse.getResult().getOutput().getText();

                    if (textChunk != null && !textChunk.isEmpty()) {
                        listener.onToken(textChunk);
                    }
                }
            },
            error -> {
                if (active.getAsBoolean()) {
                    listener.onError(error);
                }
            },
            () -> {
                if (active.getAsBoolean()) {
                    listener.onComplete(null, null);
                    // Note: Our JpaChatService orchestrator handles the final text assembly and DB saving
                }
            }
        );
    }
}
