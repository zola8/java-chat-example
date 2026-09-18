package com.example.agent.api.dto;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(

    String conversationId,
    Instant createdAt,
    List<MessageResponse> messages

) {
}
