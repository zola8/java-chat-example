package com.example.agent.api.dto.rag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IngestRequest(

    @NotBlank(message = "content is required")
    @Size(max = 100000, message = "content must be less than 100000 characters")
    String content,

    @Size(max = 500, message = "source must be less than 500 characters")
    String source,

    @Size(max = 200, message = "category must be less than 200 characters")
    String category
) {

}
