package com.example.agent.api.dto;

import java.time.Instant;

public record ChatResponse(

    String conversationId,
    String reply,
    Instant timestamp

) {

}
