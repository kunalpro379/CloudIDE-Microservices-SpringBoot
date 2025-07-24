package com.example.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    
    @Id
    private String id;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("user_id")
    private String userId;
    
    @Field("username")
    private String username;
    
    @Field("content")
    private String content;
    
    @Field("message_type")
    private String messageType = "TEXT"; // TEXT, CODE_SNIPPET, FILE_SHARE, SYSTEM_MESSAGE, EMOJI_REACTION
    
    @Field("reply_to_message_id")
    private String replyToMessageId;
    
    @Field("is_edited")
    private Boolean isEdited = false;
    
    @Field("is_deleted")
    private Boolean isDeleted = false;
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("metadata")
    private Object metadata;
    
    // Constructor for new message
    public ChatMessage(String sessionId, String userId, String username, String content, String messageType) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.messageType = messageType != null ? messageType : "TEXT";
        this.isEdited = false;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public enum MessageType {
        TEXT, CODE_SNIPPET, FILE_SHARE, SYSTEM_MESSAGE, EMOJI_REACTION
    }
}
