package com.example.agent.services;

import com.example.agent.api.dto.ChatRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.BooleanSupplier;

@Service
public class DummyStreamingChatService implements StreamingChatService {

    private static final String[] CHUNKS = {
        "Hello",
        " from",
        " the",
        " dummy",
        " streaming",
        " service."
    };

    @Override
    public void stream(
        ChatRequest request,
        BooleanSupplier active,
        StreamListener listener
    ) {

        Thread.ofVirtual()
            .name("dummy-streaming-chat")
            .start(() -> {

                try {
                    String conversationId = request.conversationId();

                    if (conversationId == null || conversationId.isBlank()) {
                        conversationId = UUID.randomUUID().toString();
                    }

                    StringBuilder fullMessage = new StringBuilder();

                    if (!active.getAsBoolean()) {
                        return;
                    }

                    // Simulate initial thinking time
                    Thread.sleep(300);

                    for (String chunk : CHUNKS) {

                        if (!active.getAsBoolean()) {
                            return;
                        }

                        // Simulate token generation delay
                        Thread.sleep(400);

                        if (!active.getAsBoolean()) {
                            return;
                        }

                        listener.onToken(chunk);
                        fullMessage.append(chunk);
                    }

                    if (!active.getAsBoolean()) {
                        return;
                    }

                    // Simulate finalization delay
                    Thread.sleep(200);

                    listener.onComplete(
                        conversationId,
                        fullMessage.toString()
                    );

                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch (Exception exception) {
                    listener.onError(exception);
                }
            });
    }
}
