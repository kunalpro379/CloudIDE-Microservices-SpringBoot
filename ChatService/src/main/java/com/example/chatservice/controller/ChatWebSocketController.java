package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatMessageDTO;
import com.example.chatservice.dto.WebSocketMessageDTO;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    
    @MessageMapping("/chat/{sessionId}/send")
    public void sendMessage(@DestinationVariable UUID sessionId, @Payload ChatMessageDTO messageDTO) {
        try {
            log.debug("Received message for session: {}", sessionId);
            
            // Validate session ID matches
            if (!sessionId.equals(messageDTO.getSessionId())) {
                log.warn("Session ID mismatch: expected {}, got {}", sessionId, messageDTO.getSessionId());
                return;
            }
            
            // Save message to database
            ChatMessage savedMessage = chatMessageService.saveMessage(messageDTO);
            
            // Update room activity
            chatRoomService.updateRoomActivity(sessionId);
            
            // Create WebSocket message
            WebSocketMessageDTO wsMessage = new WebSocketMessageDTO(
                "MESSAGE",
                sessionId,
                messageDTO.getUserId(),
                messageDTO.getUsername(),
                savedMessage
            );
            
            // Broadcast to all users in the session
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, wsMessage);
            
        } catch (Exception e) {
            log.error("Error processing message for session: {}", sessionId, e);
        }
    }
    
    @MessageMapping("/chat/{sessionId}/typing")
    public void handleTyping(@DestinationVariable UUID sessionId, @Payload WebSocketMessageDTO typingMessage) {
        try {
            log.debug("Received typing indicator for session: {} from user: {}", sessionId, typingMessage.getUserId());
            
            // Update typing status in database
            chatRoomService.updateTypingStatus(sessionId, typingMessage.getUserId(), 
                "TYPING_START".equals(typingMessage.getType()));
            
            // Broadcast typing indicator to all users except sender
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/typing", typingMessage);
            
        } catch (Exception e) {
            log.error("Error processing typing indicator for session: {}", sessionId, e);
        }
    }
    
    @MessageMapping("/chat/{sessionId}/join")
    public void handleUserJoin(@DestinationVariable UUID sessionId, @Payload WebSocketMessageDTO joinMessage) {
        try {
            log.debug("User joining session: {} - User: {}", sessionId, joinMessage.getUserId());
            
            // Add user to chat room
            chatRoomService.addParticipant(sessionId, joinMessage.getUserId(), joinMessage.getUsername());
            
            // Broadcast user joined message
            WebSocketMessageDTO wsMessage = new WebSocketMessageDTO(
                "USER_JOINED",
                sessionId,
                joinMessage.getUserId(),
                joinMessage.getUsername(),
                joinMessage.getUsername() + " joined the chat"
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, wsMessage);
            
        } catch (Exception e) {
            log.error("Error processing user join for session: {}", sessionId, e);
        }
    }
    
    @MessageMapping("/chat/{sessionId}/leave")
    public void handleUserLeave(@DestinationVariable UUID sessionId, @Payload WebSocketMessageDTO leaveMessage) {
        try {
            log.debug("User leaving session: {} - User: {}", sessionId, leaveMessage.getUserId());
            
            // Remove user from chat room
            chatRoomService.removeParticipant(sessionId, leaveMessage.getUserId());
            
            // Broadcast user left message
            WebSocketMessageDTO wsMessage = new WebSocketMessageDTO(
                "USER_LEFT",
                sessionId,
                leaveMessage.getUserId(),
                leaveMessage.getUsername(),
                leaveMessage.getUsername() + " left the chat"
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, wsMessage);
            
        } catch (Exception e) {
            log.error("Error processing user leave for session: {}", sessionId, e);
        }
    }
}
