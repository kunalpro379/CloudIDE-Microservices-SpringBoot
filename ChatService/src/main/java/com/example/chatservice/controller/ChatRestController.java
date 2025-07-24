package com.example.chatservice.controller;

import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.entity.ChatParticipant;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class ChatRestController {
    
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Page<ChatMessage>> getMessages(
            @PathVariable UUID sessionId,
            Pageable pageable) {
        
        log.debug("Fetching messages for session: {}", sessionId);
        Page<ChatMessage> messages = chatMessageService.getMessagesBySession(sessionId, pageable);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/sessions/{sessionId}/messages/recent")
    public ResponseEntity<List<ChatMessage>> getRecentMessages(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) String since) {
        
        LocalDateTime sinceTime = since != null ? 
            LocalDateTime.parse(since) : 
            LocalDateTime.now().minusMinutes(30);
            
        List<ChatMessage> messages = chatMessageService.getRecentMessages(sessionId, sinceTime);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/sessions/{sessionId}/participants")
    public ResponseEntity<List<ChatParticipant>> getOnlineParticipants(@PathVariable UUID sessionId) {
        log.debug("Fetching online participants for session: {}", sessionId);
        List<ChatParticipant> participants = chatRoomService.getOnlineParticipants(sessionId);
        return ResponseEntity.ok(participants);
    }
    
    @PostMapping("/sessions/{sessionId}/room")
    public ResponseEntity<ChatRoom> createRoom(
            @PathVariable UUID sessionId,
            @RequestParam String roomName,
            @RequestParam(required = false) String description) {
        
        log.debug("Creating chat room for session: {}", sessionId);
        ChatRoom room = chatRoomService.createRoom(sessionId, roomName, description);
        return ResponseEntity.ok(room);
    }
    
    @GetMapping("/sessions/{sessionId}/room")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable UUID sessionId) {
        log.debug("Fetching chat room for session: {}", sessionId);
        ChatRoom room = chatRoomService.getRoomBySessionId(sessionId);
        return ResponseEntity.ok(room);
    }
    
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessage> updateMessage(
            @PathVariable UUID messageId,
            @RequestBody String content) {
        
        log.debug("Updating message: {}", messageId);
        ChatMessage updatedMessage = chatMessageService.updateMessage(messageId, content);
        return ResponseEntity.ok(updatedMessage);
    }
    
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        log.debug("Deleting message: {}", messageId);
        chatMessageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/sessions/{sessionId}/stats")
    public ResponseEntity<Object> getChatStats(@PathVariable UUID sessionId) {
        log.debug("Fetching chat stats for session: {}", sessionId);
        
        Long messageCount = chatMessageService.getMessageCount(sessionId);
        List<ChatParticipant> onlineParticipants = chatRoomService.getOnlineParticipants(sessionId);
        
        return ResponseEntity.ok(new Object() {
            public final Long totalMessages = messageCount;
            public final Integer onlineParticipantCount = onlineParticipants.size();
            public final LocalDateTime timestamp = LocalDateTime.now();
        });
    }
}
