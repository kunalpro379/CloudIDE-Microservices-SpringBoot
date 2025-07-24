package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {
    
    private String type; // MESSAGE, USER_JOINED, USER_LEFT, TYPING_START, TYPING_STOP, etc.
    private UUID sessionId;
    private UUID userId;
    private String username;
    private Object payload; // Could be ChatMessageDTO, typing indicator, etc.
    private Long timestamp;
    
    public WebSocketMessageDTO(String type, UUID sessionId, UUID userId, String username, Object payload) {
        this.type = type;
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}
