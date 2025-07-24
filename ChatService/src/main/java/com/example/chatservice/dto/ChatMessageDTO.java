package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private UUID messageId;
    private UUID sessionId;
    private UUID userId;
    private String username;
    private String content;
    private String messageType;
    private UUID replyToMessageId;
    private Boolean isEdited;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metadata;
    
    // Constructor for outgoing messages
    public ChatMessageDTO(UUID sessionId, UUID userId, String username, String content, String messageType) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.messageType = messageType;
        this.isEdited = false;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
    }
}
