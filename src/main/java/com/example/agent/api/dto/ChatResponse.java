package com.example.agent.api.dto;

import java.time.Instant;


public record ChatResponse(
    String reply,
    Instant timestamp
) {
}
