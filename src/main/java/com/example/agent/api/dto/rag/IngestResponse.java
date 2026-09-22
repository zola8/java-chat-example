package com.example.agent.api.dto.rag;

import java.time.Instant;

public record IngestResponse(
    String status,
    int totalChunks,
    String source,
    Instant ingestedAt
) {
}
