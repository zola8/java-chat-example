package com.example.agent.api.dto;

public record ChatStreamDoneEvent(

    String conversationId,
    String fullMessage

) {
}
