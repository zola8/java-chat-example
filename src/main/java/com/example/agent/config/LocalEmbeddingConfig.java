package com.example.agent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LocalEmbeddingConfig {

    @Value("${app.ollama.local.base-url}")
    private String localBaseUrl;

    @Value("${app.ollama.local.embedding-model}")
    private String embeddingModel;

    private OllamaApi createLocalOllamaApi() {
        RestClient.Builder restClientBuilder = RestClient.builder()
            .defaultHeader("Content-Type", "application/json");

        WebClient.Builder webClientBuilder = WebClient.builder()
            .defaultHeader("Content-Type", "application/json");

        return OllamaApi.builder()
            .baseUrl(localBaseUrl)
            .restClientBuilder(restClientBuilder)
            .webClientBuilder(webClientBuilder)
            .build();
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
            .ollamaApi(createLocalOllamaApi())
            .defaultOptions(OllamaOptions.builder()
                .model(embeddingModel)
                .build())
            .build();
    }

}
