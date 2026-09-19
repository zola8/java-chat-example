package com.example.agent.api;


import com.example.agent.api.dto.AiPingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Ping", description = "Checks LLM provider connectivity")
public class AiPingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiPingController.class);
    private final ChatClient chatClient;

    public AiPingController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping("/ping")
    @Operation(
        summary = "Ping Ollama model",
        description = "Sends a small prompt to verify Spring AI + Ollama configuration."
    )
    public ResponseEntity<AiPingResponse> ping() {
        try {
            String answer = chatClient
                .prompt()
                .user("Answer briefly. Where is London?")
                .call()
                .content();

            return ResponseEntity.ok(new AiPingResponse("success", answer));

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            return ResponseEntity.internalServerError()
                .body(new AiPingResponse("error", exception.getMessage()));
        }
    }
}
