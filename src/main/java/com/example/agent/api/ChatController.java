package com.example.agent.api;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Minimal MVP chat endpoint")
public class ChatController {

    Logger logger = LoggerFactory.getLogger(ChatController.class);

    @PostMapping("/")
    @Operation(
        summary = "Send chat message",
        description = "MVP endpoint. Returns an echo response for now."
    )
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {

        String conversationId = request.conversationId();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String reply = "Echo: " + request.message();

        return new ChatResponse(
            conversationId,
            reply,
            Instant.now()
        );
    }


}
