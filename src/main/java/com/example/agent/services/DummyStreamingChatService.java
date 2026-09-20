package com.example.agent.services;


import com.example.agent.api.dto.ChatRequest;

import java.util.function.BooleanSupplier;

//@Service
public class DummyStreamingChatService implements StreamingChatService {

    private static final String[] CHUNKS = {
        "Hello", " from", " the", " dummy", " streaming", " service.",
        " I", " am", " now", " integrated", " with", " persistence!"
    };

    @Override
    public void stream(ChatRequest request, BooleanSupplier active, StreamListener listener) {
        try {
            Thread.sleep(300); // Simulate initial thinking

            for (String chunk : CHUNKS) {
                if (!active.getAsBoolean()) return;

                Thread.sleep(150); // Simulate token generation speed
                if (!active.getAsBoolean()) return;

                listener.onToken(chunk);
            }

            // Signal completion (Orchestrator handles the final text assembly)
            listener.onComplete(null, null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
