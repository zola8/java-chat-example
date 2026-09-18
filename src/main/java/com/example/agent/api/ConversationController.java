package com.example.agent.api;


import com.example.agent.api.dto.ConversationResponse;
import com.example.agent.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Conversations", description = "Conversation read endpoints")
public class ConversationController {

    private final ChatService chatService;

    public ConversationController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(
        value = "/{conversationId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Get conversation history",
        description = "Returns all messages for a conversation."
    )
    public ConversationResponse getConversation(@PathVariable String conversationId) {
        return chatService.getConversation(conversationId);
    }

}
