package com.example.agent.services.rag;


import com.example.agent.api.dto.rag.SearchRequest;
import com.example.agent.api.dto.rag.SearchResponse;
import com.example.agent.api.dto.rag.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentRetrievalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRetrievalService.class);

    private static final double DEFAULT_MIN_SIMILARITY_THRESHOLD = 0.40;

    private final PgVectorStore vectorStore;

    public DocumentRetrievalService(PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public SearchResponse search(SearchRequest request) {

        // TODO not everything is used, its a placeholder for a possible metadata filter
        LOGGER.debug(
            "RAG search started | query={} | topK={} | minScore={}",
            request.query(),
            request.topK(),
            request.minScore()
        );

        org.springframework.ai.vectorstore.SearchRequest aiSearchRequest =
            org.springframework.ai.vectorstore.SearchRequest.builder()
                .query(request.query())
                .topK(request.topK())
                .build();

        List<Document> results = vectorStore.similaritySearch(aiSearchRequest);

        LOGGER.debug("Raw vector search returned {} documents", results.size());

        double threshold = request.minScore() != null
            ? request.minScore()
            : DEFAULT_MIN_SIMILARITY_THRESHOLD;

        List<SearchResult> searchResults = results.stream()
            .map(this::toSearchResult)
            .filter(result -> result.score() >= threshold)
            .toList();

        LOGGER.debug(
            "RAG search completed | rawResults={} | filteredResults={} | threshold={}",
            results.size(),
            searchResults.size(),
            threshold
        );

        return new SearchResponse(
            "success",
            request.query(),
            searchResults.size(),
            searchResults
        );
    }

    private SearchResult toSearchResult(Document document) {

        String content = document.getText() != null
            ? document.getText()
            : "";

        double distance = 1.0;

        Object distanceObj = document.getMetadata().get("distance");

        if (distanceObj instanceof Number number) {
            distance = number.doubleValue();
        }

        double similarity = 1.0 - distance;

        LOGGER.debug(
            "Mapped result | distance={} | similarity={} | contentPreview={}",
            distance,
            similarity,
            truncate(content, 200)
        );

        return new SearchResult(
            content,
            (String) document.getMetadata().getOrDefault("source", "unknown"),
            (String) document.getMetadata().getOrDefault("category", "general"),
            document.getMetadata().containsKey("chunkIndex")
                ? ((Number) document.getMetadata().get("chunkIndex")).intValue()
                : -1,
            similarity
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        return value.length() <= maxLength
            ? value
            : value.substring(0, maxLength) + "...";
    }


    /**
     * Internal method used by the Chat Service to retrieve context for the AI prompt.
     */
    public List<SearchResult> findRelevantContext(String query, int topK) {
        // We reuse the existing search logic, including the threshold and similarity conversion!
        // We pass null for category/source/minScore to rely on the default threshold.
        SearchRequest internalRequest = new SearchRequest(query, topK, null, null, null);

        SearchResponse response = this.search(internalRequest);
        return response.results();
    }

}
