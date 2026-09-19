package com.example.agent.config;

import com.example.agent.exception.ConversationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleConversationNotFound(
        ConversationNotFoundException exception
    ) {
        ProblemDetail problemDetail =
            ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

        problemDetail.setTitle("Conversation not found");
        LOGGER.debug("Exception: {}", problemDetail.getDetail());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
