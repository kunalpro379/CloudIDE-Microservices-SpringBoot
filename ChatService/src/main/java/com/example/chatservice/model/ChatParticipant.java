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
@Document(collection = "chat_participants")
public class ChatParticipant {
    
    @Id
    private String id;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("user_id")
    private String userId;
    
    @Field("username")
    private String username;
    
    @Field("email")
    private String email;
    
    @Field("role")
    private String role = "PARTICIPANT"; // MODERATOR, PARTICIPANT, OBSERVER
    
    @Field("is_online")
    private Boolean isOnline = false;
    
    @Field("is_typing")
    private Boolean isTyping = false;
    
    @Field("joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
    
    @Field("last_seen")
    private LocalDateTime lastSeen;
    
    @Field("last_message_read_id")
    private String lastMessageReadId;
    
    // Constructor for new participant
    public ChatParticipant(String sessionId, String userId, String username, String email) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = "PARTICIPANT";
        this.isOnline = true;
        this.isTyping = false;
        this.joinedAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }
    
    public enum Role {
        MODERATOR, PARTICIPANT, OBSERVER
    }
}
