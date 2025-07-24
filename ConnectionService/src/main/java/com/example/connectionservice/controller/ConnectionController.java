package com.example.connectionservice.controller;

import com.example.connectionservice.entity.SessionConnection;
import com.example.connectionservice.service.ConnectionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "*")
public class ConnectionController {

    @Autowired
    private ConnectionManagerService connectionManagerService;

    @GetMapping("/{sessionId}/active")
    public ResponseEntity<List<SessionConnection>> getActiveConnections(@PathVariable String sessionId) {
        List<SessionConnection> connections = connectionManagerService.getActiveConnections(sessionId);
        return ResponseEntity.ok(connections);
    }

    @PostMapping("/{sessionId}/broadcast")
    public ResponseEntity<String> broadcastToSession(@PathVariable String sessionId, 
                                                   @RequestBody Map<String, Object> message) {
        // Implement broadcast logic
        return ResponseEntity.ok("Message broadcasted to session: " + sessionId);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupStaleConnections() {
        connectionManagerService.cleanupStaleConnections();
        return ResponseEntity.ok("Stale connections cleaned up");
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@PathVariable String sessionId) {
        List<SessionConnection> connections = connectionManagerService.getActiveConnections(sessionId);
        Map<String, Object> status = Map.of(
            "sessionId", sessionId,
            "activeConnections", connections.size(),
            "connections", connections
        );
        return ResponseEntity.ok(status);
    }
}
