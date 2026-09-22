package com.example.agent.api;


import com.example.agent.api.dto.rag.IngestRequest;
import com.example.agent.api.dto.rag.IngestResponse;
import com.example.agent.api.dto.rag.SearchRequest;
import com.example.agent.api.dto.rag.SearchResponse;
import com.example.agent.services.rag.DocumentIngestionService;
import com.example.agent.services.rag.DocumentRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG", description = "Document ingestion and retrieval endpoints")
public class RagController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagController.class);

    private final DocumentIngestionService ingestionService;
    private final DocumentRetrievalService retrievalService;

    public RagController(
        DocumentIngestionService ingestionService,
        DocumentRetrievalService retrievalService
    ) {
        this.ingestionService = ingestionService;
        this.retrievalService = retrievalService;
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
        description = "Performs vector similarity search against stored documents."
    )
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {

        LOGGER.debug("RAG search HTTP request received | query={}", request.query());

        SearchResponse response = retrievalService.search(request);

        return ResponseEntity.ok(response);
    }

}
