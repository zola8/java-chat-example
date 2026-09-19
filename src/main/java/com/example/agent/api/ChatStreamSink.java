package com.example.agent.api;


public interface ChatStreamSink {

    void sendToken(String token);

    void sendDone(String conversationId, String fullMessage);

    void sendError(String code, String message);

    void close();

    boolean isActive();
}
