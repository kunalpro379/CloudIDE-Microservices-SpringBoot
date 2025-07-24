package com.example.chatservice.repository;

import com.example.chatservice.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    Page<ChatMessage> findBySessionIdAndIsDeletedFalseOrderByCreatedAtAsc(String sessionId, Pageable pageable);
    
    List<ChatMessage> findBySessionIdAndIsDeletedFalseOrderByCreatedAtDesc(String sessionId);
    
    List<ChatMessage> findBySessionIdAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtAsc(String sessionId, LocalDateTime since);
    
    @Query("{'session_id': ?0, 'user_id': ?1, 'is_deleted': false}")
    List<ChatMessage> findBySessionIdAndUserIdAndIsDeletedFalse(String sessionId, String userId);
    
    @Query("{'session_id': ?0, 'message_type': ?1, 'is_deleted': false}")
    List<ChatMessage> findBySessionIdAndMessageTypeAndIsDeletedFalse(String sessionId, ChatMessage.MessageType messageType);
    
    long countBySessionIdAndIsDeletedFalse(String sessionId);
    
    List<ChatMessage> findByReplyToMessageIdAndIsDeletedFalse(String replyToMessageId);
    
    @Query("{'user_id': ?0, 'session_id': ?1, 'is_deleted': false}")
    Page<ChatMessage> findUserMessagesInSession(String userId, String sessionId, Pageable pageable);
    
    // Reactive methods for real-time operations
    @Query("{'session_id': ?0, 'is_deleted': false}")
    Flux<ChatMessage> findBySessionIdReactive(String sessionId);
    
    Mono<ChatMessage> findByMessageIdAndIsDeletedFalse(String messageId);
}
