package com.example.agent.api.dto.rag;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequest(

    @NotBlank(message = "query is required")
    @Size(max = 5000, message = "query must be less than 5000 characters")
    String query,

    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 50, message = "topK must be at most 50")
    int topK,

    @Size(max = 200, message = "category filter must be less than 200 characters")
    String category,

    @Size(max = 500, message = "source filter must be less than 500 characters")
    String source,

    // New: Optional threshold (0.0 to 1.0). If null, we default to 0.60
    Double minScore
) {

    // Default constructor for topK
    public SearchRequest {
        if (topK <= 0) {
            topK = 5;
        }
    }
}
