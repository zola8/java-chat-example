package com.example.agent.persistence.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class ConversationEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 64)
    private String id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
        mappedBy = "conversation",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("createdAt ASC")
    private List<MessageEntity> messages = new ArrayList<>();

    protected ConversationEntity() {
        // Required by JPA
    }

    public ConversationEntity(String id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public void addMessage(MessageEntity message) {
        messages.add(message);
        message.setConversation(this);
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<MessageEntity> getMessages() {
        return messages;
    }
}
