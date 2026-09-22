package com.example.agent.api;


import com.example.agent.api.dto.rag.IngestRequest;
import com.example.agent.api.dto.rag.IngestResponse;
import com.example.agent.api.dto.rag.SearchResponse;
import com.example.agent.api.dto.rag.SearchResult;
import com.example.agent.services.rag.DocumentIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG", description = "Document ingestion and retrieval endpoints")
public class RagController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagController.class);

    double MIN_SIMILARITY_THRESHOLD = 0.50;

    private final DocumentIngestionService ingestionService;
    private final PgVectorStore vectorStore;

    public RagController(
        DocumentIngestionService ingestionService,
        PgVectorStore vectorStore
    ) {
        this.ingestionService = ingestionService;
        this.vectorStore = vectorStore;
    }


    @PostMapping(
        value = "/ingest",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Ingest a document",
        description = "Splits the document into chunks, generates embeddings, and stores in pgvector."
    )
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody IngestRequest request) {
        LOGGER.debug("RAG ingest request received | category={}", request.category());
        IngestResponse response = ingestionService.ingest(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping(
        value = "/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Search documents by similarity",
        description = "Performs vector similarity search with optional metadata filtering and thresholding."
    )
    public ResponseEntity<SearchResponse> search(
        @Valid @RequestBody com.example.agent.api.dto.rag.SearchRequest request
    ) {
        LOGGER.debug("RAG search request | query={} | topK={} | category={} | source={}",
            request.query(), request.topK(), request.category(), request.source());

        // 2. Build Spring AI SearchRequest
        org.springframework.ai.vectorstore.SearchRequest.Builder aiSearchBuilder =
            org.springframework.ai.vectorstore.SearchRequest.builder()
                .query(request.query())
                .topK(request.topK());

        LOGGER.debug("No metadata filter applied");

        // 3. Execute Vector Search
        List<Document> results = vectorStore.similaritySearch(aiSearchBuilder.build());

        LOGGER.debug("Raw vector search returned {} documents", results.size());

        for (Document doc : results) {
            LOGGER.debug("Raw result metadata: {}", doc.getMetadata());
            LOGGER.debug("Raw result text preview: {}", doc.getText());
        }

        // 4. Map + threshold
        double threshold = request.minScore() != null
            ? request.minScore()
            : MIN_SIMILARITY_THRESHOLD;


        List<SearchResult> searchResults = results.stream()
            .map(doc -> {
                String content = doc.getText() != null ? doc.getText() : "";

                double distance = 1.0;
                Object distanceObj = doc.getMetadata().get("distance");

                if (distanceObj instanceof Number number) {
                    distance = number.doubleValue();
                }

                double similarity = 1.0 - distance;

                LOGGER.debug(
                    "Mapped result | distance={} | similarity={} | contentPreview={}",
                    distance,
                    similarity,
                    content
                );

                return new SearchResult(
                    content,
                    (String) doc.getMetadata().getOrDefault("source", "unknown"),
                    (String) doc.getMetadata().getOrDefault("category", "general"),
                    doc.getMetadata().containsKey("chunkIndex")
                        ? ((Number) doc.getMetadata().get("chunkIndex")).intValue()
                        : -1,
                    similarity
                );
            })
            .filter(result -> result.score() >= threshold)
            .toList();

        LOGGER.debug(
            "RAG search completed | rawResults={} | filteredResults={} | threshold={}",
            results.size(),
            searchResults.size(),
            threshold
        );

        return ResponseEntity.ok(new SearchResponse(
            "success",
            request.query(),
            searchResults.size(),
            searchResults
        ));
    }

}
