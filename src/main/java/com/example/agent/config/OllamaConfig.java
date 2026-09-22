package com.example.agent.config;


import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaConfig {

    @Value("${OLLAMA_API_KEY}")
    private String apiKey;

    @Bean
    public OllamaApi ollamaApi() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OLLAMA_API_KEY environment variable is not set!");
        }

        // 1. RestClient for synchronous calls (.call())
        RestClient.Builder restClientBuilder = RestClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        // 2. WebClient for streaming calls (.stream())
        WebClient.Builder webClientBuilder = WebClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        return OllamaApi.builder()
            .baseUrl("https://ollama.com")
            .restClientBuilder(restClientBuilder)
            .webClientBuilder(webClientBuilder)
            .build();
    }

    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(OllamaOptions.builder()
                .model("gemma4:31b")
                .temperature(0.1)
                .build())
            .build();
    }

}
