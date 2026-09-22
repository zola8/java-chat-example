package com.example.agent.api.dto.rag;


import java.util.List;

public record SearchResponse(
    String status,
    String query,
    int resultCount,
    List<SearchResult> results
) {
}
