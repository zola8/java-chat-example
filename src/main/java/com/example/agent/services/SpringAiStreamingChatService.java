package com.example.agent.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.BooleanSupplier;

@Service
public class SpringAiStreamingChatService implements StreamingChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAiStreamingChatService.class);

    private final ChatClient chatClient;

    public SpringAiStreamingChatService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public void stream(List<Message> messages, BooleanSupplier active, StreamListener listener) {

        LOGGER.debug("Starting streaming LLM call | messageCount={}", messages.size());

        Flux<ChatResponse> flux = chatClient.prompt()
            .messages(messages)
            .stream()
            .chatResponse();

        flux.subscribe(
            chatResponse -> {
                if (!active.getAsBoolean()) {
                    return;
                }

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
