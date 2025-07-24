package com.example.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {
    
    @Id
    private String id;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("room_name")
    private String roomName;
    
    @Field("description")
    private String description;
    
    @Field("is_active")
    private Boolean isActive = true;
    
    @Field("max_participants")
    private Integer maxParticipants = 50;
    
    @Field("current_participants")
    private Integer currentParticipants = 0;
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("last_activity")
    private LocalDateTime lastActivity = LocalDateTime.now();
    
    @Field("settings")
    private Object settings;
    
    // Constructor for creating new room
    public ChatRoom(String sessionId, String roomName, String description) {
        this.sessionId = sessionId;
        this.roomName = roomName;
        this.description = description;
        this.isActive = true;
        this.maxParticipants = 50;
        this.currentParticipants = 0;
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
}
