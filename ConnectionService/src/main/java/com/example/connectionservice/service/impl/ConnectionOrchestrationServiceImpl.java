package com.example.connectionservice.service.impl;

import com.example.connectionservice.model.SessionConnection;
import com.example.connectionservice.repository.SessionConnectionRepository;
import com.example.connectionservice.service.ConnectionOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionOrchestrationServiceImpl implements ConnectionOrchestrationService {

    private final SessionConnectionRepository sessionConnectionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;

    @Override
    public SessionConnection createSession(String sessionId) {
        log.info("Creating new session: {}", sessionId);
        
        Optional<SessionConnection> existing = sessionConnectionRepository.findBySessionId(sessionId);
        if (existing.isPresent()) {
            log.info("Session already exists: {}", sessionId);
            return existing.get();
        }

        SessionConnection connection = new SessionConnection(sessionId);
        SessionConnection saved = sessionConnectionRepository.save(connection);
        
        log.info("Session created successfully: {}", sessionId);
        return saved;
    }

    @Override
    public Optional<SessionConnection> getSession(String sessionId) {
        return sessionConnectionRepository.findBySessionId(sessionId);
    }

    @Override
    public boolean joinSession(String sessionId, String userId) {
        log.info("User {} joining session: {}", userId, sessionId);
        
        Optional<SessionConnection> sessionOpt = sessionConnectionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found: {}", sessionId);
            return false;
        }

        SessionConnection session = sessionOpt.get();
        if (!session.canAcceptNewConnection()) {
            log.warn("Session {} is at maximum capacity", sessionId);
            return false;
        }

        session.addUser(userId);
        sessionConnectionRepository.save(session);
        
        // Notify chat and CRDT services
        notifyServicesUserJoined(sessionId, userId);
        
        // Broadcast to other users in session
        broadcastToSession(sessionId, new SessionEventMessage("USER_JOINED", userId, session.getCurrentConnections()));
        
        log.info("User {} successfully joined session: {}", userId, sessionId);
        return true;
    }

    @Override
    public boolean leaveSession(String sessionId, String userId) {
        log.info("User {} leaving session: {}", userId, sessionId);
        
        Optional<SessionConnection> sessionOpt = sessionConnectionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found: {}", sessionId);
            return false;
        }

        SessionConnection session = sessionOpt.get();
        session.removeUser(userId);
        sessionConnectionRepository.save(session);
        
        // Notify chat and CRDT services
        notifyServicesUserLeft(sessionId, userId);
        
        // Broadcast to other users in session
        broadcastToSession(sessionId, new SessionEventMessage("USER_LEFT", userId, session.getCurrentConnections()));
        
        log.info("User {} successfully left session: {}", userId, sessionId);
        return true;
    }

    @Override
    public List<SessionConnection> getActiveSessions() {
        return sessionConnectionRepository.findByIsActiveTrue();
    }

    @Override
    public void enableChatForSession(String sessionId) {
        updateSessionService(sessionId, "chat", true);
    }

    @Override
    public void disableChatForSession(String sessionId) {
        updateSessionService(sessionId, "chat", false);
    }

    @Override
    public void enableCRDTForSession(String sessionId) {
        updateSessionService(sessionId, "crdt", true);
    }

    @Override
    public void disableCRDTForSession(String sessionId) {
        updateSessionService(sessionId, "crdt", false);
    }

    @Override
    public boolean isChatAvailable(String sessionId) {
        return getSession(sessionId)
                .map(SessionConnection::getChatEnabled)
                .orElse(false);
    }

    @Override
    public boolean isCRDTAvailable(String sessionId) {
        return getSession(sessionId)
                .map(SessionConnection::getCrdtEnabled)
                .orElse(false);
    }

    @Override
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<SessionConnection> inactiveSessions = sessionConnectionRepository.findByLastActivityBefore(cutoffTime);
        
        for (SessionConnection session : inactiveSessions) {
            if (session.getCurrentConnections() == 0) {
                session.setIsActive(false);
                sessionConnectionRepository.save(session);
                log.info("Deactivated inactive session: {}", session.getSessionId());
            }
        }
    }

    @Override
    public void broadcastToSession(String sessionId, Object message) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    @Override
    public void forwardToChatService(String sessionId, Object message) {
        try {
            Optional<SessionConnection> sessionOpt = getSession(sessionId);
            if (sessionOpt.isPresent() && sessionOpt.get().getChatEnabled()) {
                String chatServiceUrl = sessionOpt.get().getChatServiceUrl();
                restTemplate.postForEntity(chatServiceUrl + "/api/chat/forward", message, String.class);
            }
        } catch (Exception e) {
            log.error("Failed to forward message to chat service for session: {}", sessionId, e);
        }
    }

    @Override
    public void forwardToCRDTService(String sessionId, Object message) {
        try {
            Optional<SessionConnection> sessionOpt = getSession(sessionId);
            if (sessionOpt.isPresent() && sessionOpt.get().getCrdtEnabled()) {
                String crdtServiceUrl = sessionOpt.get().getCrdtServiceUrl();
                restTemplate.postForEntity(crdtServiceUrl + "/api/crdt/forward", message, String.class);
            }
        } catch (Exception e) {
            log.error("Failed to forward message to CRDT service for session: {}", sessionId, e);
        }
    }

    private void updateSessionService(String sessionId, String serviceType, boolean enabled) {
        Optional<SessionConnection> sessionOpt = getSession(sessionId);
        if (sessionOpt.isPresent()) {
            SessionConnection session = sessionOpt.get();
            if ("chat".equals(serviceType)) {
                session.setChatEnabled(enabled);
            } else if ("crdt".equals(serviceType)) {
                session.setCrdtEnabled(enabled);
            }
            sessionConnectionRepository.save(session);
            log.info("Updated {} service for session {}: {}", serviceType, sessionId, enabled);
        }
    }

    private void notifyServicesUserJoined(String sessionId, String userId) {
        // Notify chat service
        try {
            UserEventMessage userEvent = new UserEventMessage("JOIN", sessionId, userId);
            forwardToChatService(sessionId, userEvent);
        } catch (Exception e) {
            log.warn("Failed to notify chat service of user join: {}", e.getMessage());
        }

        // Notify CRDT service
        try {
            UserEventMessage userEvent = new UserEventMessage("JOIN", sessionId, userId);
            forwardToCRDTService(sessionId, userEvent);
        } catch (Exception e) {
            log.warn("Failed to notify CRDT service of user join: {}", e.getMessage());
        }
    }

    private void notifyServicesUserLeft(String sessionId, String userId) {
        // Notify chat service
        try {
            UserEventMessage userEvent = new UserEventMessage("LEAVE", sessionId, userId);
            forwardToChatService(sessionId, userEvent);
        } catch (Exception e) {
            log.warn("Failed to notify chat service of user leave: {}", e.getMessage());
        }

        // Notify CRDT service
        try {
            UserEventMessage userEvent = new UserEventMessage("LEAVE", sessionId, userId);
            forwardToCRDTService(sessionId, userEvent);
        } catch (Exception e) {
            log.warn("Failed to notify CRDT service of user leave: {}", e.getMessage());
        }
    }

    // Inner classes for message types
    public static class SessionEventMessage {
        private String type;
        private String userId;
        private Integer connectionCount;

        public SessionEventMessage(String type, String userId, Integer connectionCount) {
            this.type = type;
            this.userId = userId;
            this.connectionCount = connectionCount;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Integer getConnectionCount() { return connectionCount; }
        public void setConnectionCount(Integer connectionCount) { this.connectionCount = connectionCount; }
    }

    public static class UserEventMessage {
        private String action;
        private String sessionId;
        private String userId;

        public UserEventMessage(String action, String sessionId, String userId) {
            this.action = action;
            this.sessionId = sessionId;
            this.userId = userId;
        }

        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
