package com.example.agent.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorStoreConfig {

    @Bean
    public PgVectorStore vectorStore(
        JdbcTemplate jdbcTemplate,
        EmbeddingModel embeddingModel
    ) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .schemaName("public")
            .vectorTableName("vector_store")
            .dimensions(1024) // mxbai-embed-large produces 1024 dimensions
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .removeExistingVectorStoreTable(false)
            .initializeSchema(true) // Auto-create table for MVP
            .build();
    }

}
