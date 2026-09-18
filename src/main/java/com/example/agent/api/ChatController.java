package com.example.agent.api;


import com.example.agent.api.dto.ChatRequest;
import com.example.agent.api.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Minimal MVP chat endpoint")
public class ChatController {

    Logger logger = LoggerFactory.getLogger(ChatController.class);

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Echo chat message",
        description = "Temporary MVP endpoint. Will later be replaced by agent orchestration."
    )
    public ChatResponse chat(@RequestBody ChatRequest request) {
        logger.debug("Request: {}", request.message());
        String message = request.message() == null ? "" : request.message();

        return new ChatResponse(
            "Echo: " + message,
            Instant.now()
        );
    }
}
