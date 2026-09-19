package com.example.agent.api;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import com.example.agent.services.ChatService;
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


    @PostMapping(
        value = "/",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Send chat message")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }


    @PostMapping(
        value = "/stream",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @Operation(summary = "Stream chat response (Asynchronous SSE)")
    public SseEmitter stream(@Valid @RequestBody ChatRequest request) {

        // 1. Create HTTP Emitter (60 seconds timeout)
        SseEmitter emitter = new SseEmitter(60_000L);

        // 2. Wrap it in our Application Layer Sink
        ChatStreamSink sink = new SseEmitterSink(emitter);

        // 3. Hand off to the Service Orchestrator
        chatService.streamChat(request, sink);

        // 4. Return immediately to Tomcat
        return emitter;
    }
}
