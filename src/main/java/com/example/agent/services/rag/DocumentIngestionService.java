package com.example.agent.services.rag;


import com.example.agent.api.dto.rag.IngestRequest;
import com.example.agent.api.dto.rag.IngestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final PgVectorStore vectorStore;
    private final TextSplitter textSplitter;

    public DocumentIngestionService(
        PgVectorStore vectorStore,
        TextSplitter textSplitter
    ) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    public IngestResponse ingest(IngestRequest request) {
        String documentId = UUID.randomUUID().toString();
        String source = request.source() != null ? request.source() : "unknown";
        String category = request.category() != null ? request.category() : "general";

        LOGGER.debug(
            "Starting document ingestion | documentId={} | source={} | contentLength={}",
            documentId,
            source,
            request.content().length()
        );

        // 1. Create the source document
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId);
        metadata.put("source", source);
        metadata.put("category", category);
        metadata.put("ingestedAt", Instant.now().toString());

        Document sourceDocument = new Document(request.content(), metadata);

        // 2. Split into chunks
        List<Document> chunks = textSplitter.apply(List.of(sourceDocument));

        LOGGER.debug(
            "Document split into chunks | documentId={} | chunkCount={}",
            documentId,
            chunks.size()
        );

        // 3. Add chunk-specific metadata to each chunk
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            chunk.getMetadata().put("chunkIndex", i);
            chunk.getMetadata().put("totalChunks", chunks.size());
        }

        // 4. Store in pgvector (embeddings generated automatically)
        vectorStore.add(chunks);

        LOGGER.debug(
            "Document ingestion complete | documentId={} | chunksStored={}",
            documentId,
            chunks.size()
        );

        return new IngestResponse(
            "success",
            chunks.size(),
            source,
            Instant.now()
        );
    }

}
