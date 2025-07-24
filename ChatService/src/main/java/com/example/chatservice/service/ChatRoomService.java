package com.example.chatservice.service;

import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.entity.ChatParticipant;

import java.util.List;
import java.util.UUID;

public interface ChatRoomService {
    
    ChatRoom createRoom(UUID sessionId, String roomName, String description);
    
    ChatRoom getRoomBySessionId(UUID sessionId);
    
    void addParticipant(UUID sessionId, UUID userId, String username);
    
    void removeParticipant(UUID sessionId, UUID userId);
    
    List<ChatParticipant> getOnlineParticipants(UUID sessionId);
    
    void updateTypingStatus(UUID sessionId, UUID userId, boolean isTyping);
    
    void updateRoomActivity(UUID sessionId);
    
    void deactivateRoom(UUID sessionId);
    
    List<ChatRoom> getInactiveRooms();
}
