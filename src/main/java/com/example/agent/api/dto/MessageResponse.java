package com.example.agent.api.dto;

import com.example.agent.domain.Role;

import java.time.Instant;

public record MessageResponse(

    Role role,
    String content,
    Instant createdAt

) {
}
