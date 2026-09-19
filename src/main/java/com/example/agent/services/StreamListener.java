package com.example.agent.services;


public interface StreamListener {

    void onToken(String token);

    void onComplete(String conversationId, String fullMessage);

    void onError(Throwable error);
}
