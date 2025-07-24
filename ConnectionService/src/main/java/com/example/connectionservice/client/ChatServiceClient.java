package com.example.connectionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {
    
    @PostMapping("/api/chat/{sessionId}/message")
    void sendMessage(@PathVariable String sessionId, @RequestBody Map<String, Object> message);
    
    @PostMapping("/api/chat/{sessionId}/user-joined")
    void notifyUserJoined(@PathVariable String sessionId, @RequestBody Map<String, Object> userInfo);
    
    @PostMapping("/api/chat/{sessionId}/user-left")
    void notifyUserLeft(@PathVariable String sessionId, @RequestBody Map<String, Object> userInfo);
    
    @GetMapping("/api/chat/{sessionId}/status")
    Map<String, Object> getChatRoomStatus(@PathVariable String sessionId);
}
