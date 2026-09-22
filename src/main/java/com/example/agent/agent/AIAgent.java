package com.example.agent.agent;

import com.example.agent.tools.TimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class AIAgent {

    private final ChatClient chatClient;
    private final TimeTool timeTool;

    public AIAgent(ChatModel chatModel, TimeTool timeTool) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.timeTool = timeTool;
    }

    public String generate(List<Message> messages) {
        return chatClient.prompt()
            .messages(messages)
            .tools(timeTool)
            .call()
            .content();
    }

    public Flux<ChatResponse> stream(List<Message> messages) {
        return chatClient.prompt()
            .messages(messages)
            .tools(timeTool)
            .stream()
            .chatResponse();
    }
}
