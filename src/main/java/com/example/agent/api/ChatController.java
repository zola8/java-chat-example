package com.example.agent.api;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.api.dto.ChatStreamDoneEvent;
import com.example.agent.api.dto.ChatStreamErrorEvent;
import com.example.agent.api.dto.ChatStreamTokenEvent;
import com.example.agent.services.ChatService;
import com.example.agent.services.StreamListener;
import com.example.agent.services.StreamingChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Chat endpoint")
public class ChatController {

    private final ChatService chatService;
    private final StreamingChatService streamingChatService;

    public ChatController(
        ChatService chatService,
        StreamingChatService streamingChatService
    ) {
        this.chatService = chatService;
        this.streamingChatService = streamingChatService;
    }


    @PostMapping("/")
    @Operation(summary = "Send chat message")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }


    @PostMapping(
        value = "/stream",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @Operation(
        summary = "Stream chat response",
        description = "Dummy SSE streaming endpoint. Sends response in multiple parts."
    )
    public SseEmitter stream(@Valid @RequestBody ChatRequest request) {

        SseEmitter emitter = new SseEmitter(60_000L);

        AtomicBoolean active = new AtomicBoolean(true);

        emitter.onCompletion(() -> active.set(false));
        emitter.onTimeout(() -> active.set(false));
        emitter.onError(error -> active.set(false));

        streamingChatService.stream(
            request,
            active::get,
            new StreamListener() {

                @Override
                public void onToken(String token) {
                    if (!active.get()) {
                        return;
                    }

                    send(
                        emitter,
                        active,
                        "token",
                        new ChatStreamTokenEvent(token)
                    );
                }

                @Override
                public void onComplete(String conversationId, String fullMessage) {
                    if (!active.get()) {
                        return;
                    }

                    send(
                        emitter,
                        active,
                        "done",
                        new ChatStreamDoneEvent(conversationId, fullMessage)
                    );

                    emitter.complete();
                }

                @Override
                public void onError(Throwable error) {
                    if (!active.get()) {
                        return;
                    }

                    send(
                        emitter,
                        active,
                        "error",
                        new ChatStreamErrorEvent(
                            "STREAM_ERROR",
                            error.getMessage()
                        )
                    );

                    emitter.completeWithError(error);
                }
            }
        );

        return emitter;
    }

    private void send(
        SseEmitter emitter,
        AtomicBoolean active,
        String eventName,
        Object payload
    ) {
        try {
            emitter.send(
                SseEmitter.event()
                    .name(eventName)
                    .data(payload)
            );
        } catch (Exception exception) {
            active.set(false);
        }
    }

}
