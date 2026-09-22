package com.example.agent.config;


import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter(
            500,    // defaultChunkSize: target tokens per chunk
            200,    // minChunkSizeChars: minimum characters before splitting
            5,      // minChunkLengthToEmbed: skip chunks shorter than this
            100,    // maxNumChunks: max chunks per document
            true    // keepSeparator: keep separators in chunks
        );
    }

}
