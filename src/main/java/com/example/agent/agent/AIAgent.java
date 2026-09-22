package com.example.agent.agent;

import com.example.agent.tools.AgentTool;
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
    private final List<AgentTool> tools;

    public AIAgent(ChatModel chatModel, List<AgentTool> tools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.tools = tools;
    }

    public String generate(List<Message> messages) {
        return chatClient.prompt()
            .messages(messages)
            .tools(tools.toArray(Object[]::new))
            .call()
            .content();
    }

    public Flux<ChatResponse> stream(List<Message> messages) {
        return chatClient.prompt()
            .messages(messages)
            .tools(tools.toArray(Object[]::new))
            .stream()
            .chatResponse();
    }
}
