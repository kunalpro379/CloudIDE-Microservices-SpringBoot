package com.example.chatservice.service.impl;

import com.example.chatservice.model.ChatRoom;
import com.example.chatservice.model.ChatParticipant;
import com.example.chatservice.repository.ChatRoomRepository;
import com.example.chatservice.repository.ChatParticipantRepository;
import com.example.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    
    @Override
    public ChatRoom createRoom(String sessionId, String roomName, String description) {
        log.debug("Creating chat room for session: {}", sessionId);
        
        // Check if room already exists
        return chatRoomRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatRoom room = new ChatRoom();
                    room.setSessionId(sessionId);
                    room.setRoomName(roomName);
                    room.setDescription(description);
                    room.setIsActive(true);
                    room.setCurrentParticipants(0);
                    room.setLastActivity(LocalDateTime.now());
                    return chatRoomRepository.save(room);
                });
    }
    
    @Override
    @Transactional(readOnly = true)
    public ChatRoom getRoomBySessionId(UUID sessionId) {
        return chatRoomRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat room not found for session: " + sessionId));
    }
    
    @Override
    public void addParticipant(UUID sessionId, UUID userId, String username) {
        log.debug("Adding participant {} to session: {}", userId, sessionId);
        
        // Check if participant already exists
        chatParticipantRepository.findBySessionIdAndUserId(sessionId, userId)
                .ifPresentOrElse(
                    participant -> {
                        // Update existing participant
                        participant.setIsOnline(true);
                        participant.setLastSeen(LocalDateTime.now());
                        chatParticipantRepository.save(participant);
                    },
                    () -> {
                        // Create new participant
                        ChatParticipant participant = new ChatParticipant();
                        participant.setSessionId(sessionId);
                        participant.setUserId(userId);
                        participant.setUsername(username);
                        participant.setIsOnline(true);
                        participant.setIsTyping(false);
                        participant.setLastSeen(LocalDateTime.now());
                        chatParticipantRepository.save(participant);
                    }
                );
        
        // Update participant count
        updateParticipantCount(sessionId);
    }
    
    @Override
    public void removeParticipant(UUID sessionId, UUID userId) {
        log.debug("Removing participant {} from session: {}", userId, sessionId);
        
        chatParticipantRepository.updateOnlineStatus(sessionId, userId, false, LocalDateTime.now());
        updateParticipantCount(sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatParticipant> getOnlineParticipants(UUID sessionId) {
        return chatParticipantRepository.findBySessionIdAndIsOnlineTrue(sessionId);
    }
    
    @Override
    public void updateTypingStatus(UUID sessionId, UUID userId, boolean isTyping) {
        chatParticipantRepository.updateTypingStatus(sessionId, userId, isTyping);
    }
    
    @Override
    public void updateRoomActivity(UUID sessionId) {
        chatRoomRepository.updateLastActivity(sessionId, LocalDateTime.now());
    }
    
    @Override
    public void deactivateRoom(UUID sessionId) {
        log.debug("Deactivating chat room for session: {}", sessionId);
        
        ChatRoom room = getRoomBySessionId(sessionId);
        room.setIsActive(false);
        chatRoomRepository.save(room);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> getInactiveRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // 24 hours of inactivity
        return chatRoomRepository.findInactiveRooms(threshold);
    }
    
    private void updateParticipantCount(UUID sessionId) {
        Long count = chatParticipantRepository.countOnlineParticipants(sessionId);
        chatRoomRepository.updateParticipantCount(sessionId, count.intValue());
    }
}
