package com.example.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(

    @Size(max = 64)
    String conversationId,

    @NotBlank
    @Size(max = 8000)
    String message
) {
}
