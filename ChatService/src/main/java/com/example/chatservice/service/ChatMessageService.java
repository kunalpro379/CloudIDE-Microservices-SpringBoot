package com.example.chatservice.service;

import com.example.chatservice.dto.ChatMessageDTO;
import com.example.chatservice.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChatMessageService {
    
    ChatMessage saveMessage(ChatMessageDTO messageDTO);
    
    Page<ChatMessage> getMessagesBySession(UUID sessionId, Pageable pageable);
    
    List<ChatMessage> getRecentMessages(UUID sessionId, LocalDateTime since);
    
    ChatMessage updateMessage(UUID messageId, String content);
    
    void deleteMessage(UUID messageId);
    
    Long getMessageCount(UUID sessionId);
    
    List<ChatMessage> getReplies(UUID messageId);
    
    Page<ChatMessage> getUserMessagesInSession(UUID userId, UUID sessionId, Pageable pageable);
}
