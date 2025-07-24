package com.example.connectionservice.service;

import com.example.connectionservice.model.SessionConnection;
import java.util.List;
import java.util.Optional;

public interface ConnectionOrchestrationService {
    
    SessionConnection createSession(String sessionId);
    
    Optional<SessionConnection> getSession(String sessionId);
    
    boolean joinSession(String sessionId, String userId);
    
    boolean leaveSession(String sessionId, String userId);
    
    List<SessionConnection> getActiveSessions();
    
    void enableChatForSession(String sessionId);
    
    void disableChatForSession(String sessionId);
    
    void enableCRDTForSession(String sessionId);
    
    void disableCRDTForSession(String sessionId);
    
    boolean isChatAvailable(String sessionId);
    
    boolean isCRDTAvailable(String sessionId);
    
    void cleanupInactiveSessions();
    
    void broadcastToSession(String sessionId, Object message);
    
    void forwardToChatService(String sessionId, Object message);
    
    void forwardToCRDTService(String sessionId, Object message);
}
