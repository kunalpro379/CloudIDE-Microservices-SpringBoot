package com.example.chatservice.service.impl;

import com.example.chatservice.dto.ChatMessageDTO;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.repository.ChatMessageRepository;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    
    @Override
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        log.debug("Saving message for session: {}", messageDTO.getSessionId());
        
        ChatMessage message = new ChatMessage();
        message.setSessionId(messageDTO.getSessionId());
        message.setUserId(messageDTO.getUserId());
        message.setUsername(messageDTO.getUsername());
        message.setContent(messageDTO.getContent());
        message.setMessageType(ChatMessage.MessageType.valueOf(messageDTO.getMessageType()));
        message.setReplyToMessageId(messageDTO.getReplyToMessageId());
        message.setMetadata(messageDTO.getMetadata());
        message.setIsEdited(false);
        message.setIsDeleted(false);
        
        return chatMessageRepository.save(message);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessagesBySession(UUID sessionId, Pageable pageable) {
        log.debug("Fetching messages for session: {}", sessionId);
        return chatMessageRepository.findBySessionIdAndIsDeletedFalseOrderByCreatedAtAsc(sessionId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentMessages(UUID sessionId, LocalDateTime since) {
        log.debug("Fetching recent messages for session: {} since: {}", sessionId, since);
        return chatMessageRepository.findRecentMessages(sessionId, since);
    }
    
    @Override
    public ChatMessage updateMessage(UUID messageId, String content) {
        log.debug("Updating message: {}", messageId);
        
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        message.setContent(content);
        message.setIsEdited(true);
        message.setUpdatedAt(LocalDateTime.now());
        
        return chatMessageRepository.save(message);
    }
    
    @Override
    public void deleteMessage(UUID messageId) {
        log.debug("Deleting message: {}", messageId);
        
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        message.setIsDeleted(true);
        message.setUpdatedAt(LocalDateTime.now());
        
        chatMessageRepository.save(message);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getMessageCount(UUID sessionId) {
        return chatMessageRepository.countMessagesBySessionId(sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getReplies(UUID messageId) {
        return chatMessageRepository.findByReplyToMessageIdAndIsDeletedFalse(messageId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessage> getUserMessagesInSession(UUID userId, UUID sessionId, Pageable pageable) {
        return chatMessageRepository.findUserMessagesInSession(userId, sessionId, pageable);
    }
}
