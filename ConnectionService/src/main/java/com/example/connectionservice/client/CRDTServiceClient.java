package com.example.connectionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "crdt-service")
public interface CRDTServiceClient {
    
    @PostMapping("/api/crdt/{sessionId}/operation")
    void sendOperation(@PathVariable String sessionId, @RequestBody Map<String, Object> operation);
    
    @PostMapping("/api/crdt/{sessionId}/user-joined")
    void notifyUserJoined(@PathVariable String sessionId, @RequestBody Map<String, Object> userInfo);
    
    @PostMapping("/api/crdt/{sessionId}/user-left")
    void notifyUserLeft(@PathVariable String sessionId, @RequestBody Map<String, Object> userInfo);
    
    @GetMapping("/api/crdt/{sessionId}/document")
    Map<String, Object> getDocument(@PathVariable String sessionId);
    
    @GetMapping("/api/crdt/{sessionId}/status")
    Map<String, Object> getDocumentStatus(@PathVariable String sessionId);
}
