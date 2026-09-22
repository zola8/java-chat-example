package com.example.agent.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag/test")
@Tag(name = "RAG Test", description = "Temporary endpoints to verify RAG setup")
public class RagTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagTestController.class);

    private final PgVectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public RagTestController(PgVectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embedding")
    @Operation(summary = "Test embedding model connectivity")
    public ResponseEntity<Map<String, Object>> testEmbedding() {
        try {
            LOGGER.debug("Testing embedding model");

            float[] embedding = embeddingModel.embed("Hello, this is a test");

            LOGGER.debug("Embedding generated | dimensions={}", embedding.length);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "dimensions", embedding.length,
                "firstValues", List.of(
                    embedding[0],
                    embedding[1],
                    embedding[2]
                )
            ));

        } catch (Exception e) {
            LOGGER.error("Embedding test failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/store")
    @Operation(summary = "Store a test document in pgvector")
    public ResponseEntity<Map<String, Object>> testStore(
        @RequestBody Map<String, String> request
    ) {
        try {
            String content = request.get("content");

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "content is required"
                ));
            }

            LOGGER.debug("Storing test document | length={}", content.length());

            Document document = new Document(content);
            vectorStore.add(List.of(document));

            LOGGER.debug("Document stored successfully");

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Document stored in pgvector"
            ));

        } catch (Exception e) {
            LOGGER.error("Store test failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/search")
    @Operation(summary = "Search pgvector for similar documents")
    public ResponseEntity<Map<String, Object>> testSearch(
        @RequestBody Map<String, String> request
    ) {
        try {
            String query = request.get("query");

            if (query == null || query.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "query is required"
                ));
            }

            LOGGER.debug("Searching pgvector | query={}", query);

            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(query)
                    .topK(5)
                    .build()
            );

            LOGGER.debug("Search completed | results={}", results.size());

            List<Map<String, Object>> resultTexts = results.stream()
                .map(doc -> Map.<String, Object>of(
                    "content", doc.getFormattedContent(),
                    "score", doc.getMetadata().getOrDefault("distance", "N/A")
                ))
                .toList();

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "query", query,
                "resultCount", results.size(),
                "results", resultTexts
            ));

        } catch (Exception e) {
            LOGGER.error("Search test failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}
