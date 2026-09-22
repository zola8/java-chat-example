package com.example.agent.api.dto.rag;


public record SearchResult(
    String content,
    String source,
    String category,
    int chunkIndex,
    double score
) {
}
