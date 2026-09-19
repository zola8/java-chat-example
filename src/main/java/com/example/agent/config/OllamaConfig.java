package com.example.agent.config;


import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaConfig {

    @Bean
    public OllamaApi ollamaApi() {
        String apiKey = System.getenv("OLLAMA_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OLLAMA_API_KEY environment variable is not set!");
        }

        RestClient.Builder restClientBuilder = RestClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        return OllamaApi.builder()
            .baseUrl("https://ollama.com")
            .restClientBuilder(restClientBuilder)
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
