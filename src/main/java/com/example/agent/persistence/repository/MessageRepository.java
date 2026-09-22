package com.example.agent.persistence.repository;

import com.example.agent.persistence.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<MessageEntity> findByConversationId(String conversationId, Pageable pageable);

}
