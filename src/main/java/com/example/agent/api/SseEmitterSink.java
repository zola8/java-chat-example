package com.example.agent.api;


import com.example.agent.api.dto.ChatStreamDoneEvent;
import com.example.agent.api.dto.ChatStreamErrorEvent;
import com.example.agent.api.dto.ChatStreamTokenEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

public class SseEmitterSink implements ChatStreamSink {

    private final SseEmitter emitter;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public SseEmitterSink(SseEmitter emitter) {
        this.emitter = emitter;

        // Handle client disconnects and timeouts
        emitter.onCompletion(() -> active.set(false));
        emitter.onTimeout(() -> active.set(false));
        emitter.onError(error -> active.set(false));
    }

    @Override
    public void sendToken(String token) {
        if (active.get()) {
            send("token", new ChatStreamTokenEvent(token));
        }
    }

    @Override
    public void sendDone(String conversationId, String fullMessage) {
        if (active.get()) {
            send("done", new ChatStreamDoneEvent(conversationId, fullMessage));
        }
    }

    @Override
    public void sendError(String code, String message) {
        if (active.get()) {
            send("error", new ChatStreamErrorEvent(code, message));
        }
    }

    @Override
    public void close() {
        if (active.getAndSet(false)) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // Ignore exceptions during close
            }
        }
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    private void send(String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (Exception e) {
            // If sending fails (e.g. client disconnected), mark as inactive
            active.set(false);
        }
    }
}
