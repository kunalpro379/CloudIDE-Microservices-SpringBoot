package com.example.connectionservice.service;

import com.example.connectionservice.entity.SessionConnection;
import com.example.connectionservice.entity.ServiceCommunication;
import com.example.connectionservice.repository.SessionConnectionRepository;
import com.example.connectionservice.repository.ServiceCommunicationRepository;
import com.example.connectionservice.client.ChatServiceClient;
import com.example.connectionservice.client.CRDTServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ConnectionManagerService {

    @Autowired
    private SessionConnectionRepository connectionRepository;

    @Autowired
    private ServiceCommunicationRepository communicationRepository;

    @Autowired
    private ChatServiceClient chatServiceClient;

    @Autowired
    private CRDTServiceClient crdtServiceClient;

    public void handleUserConnected(String sessionId, String userId, String websocketSessionId) {
        log.info("User {} connected to session {}", userId, sessionId);
        
        // Create or update connection record
        SessionConnection connection = connectionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElse(new SessionConnection());
        
        connection.setSessionId(sessionId);
        connection.setUserId(userId);
        connection.setWebsocketSessionId(websocketSessionId);
        connection.setStatus(SessionConnection.ConnectionStatus.CONNECTED);
        connection.setServicesConnected(new HashSet<>());
        connection.setLastActivity(LocalDateTime.now());
        connection.setLastHeartbeat(LocalDateTime.now());
        
        connectionRepository.save(connection);
        
        // Notify services about user connection
        notifyServiceUserJoined(sessionId, userId, "chat-service");
        notifyServiceUserJoined(sessionId, userId, "crdt-service");
    }

    public void handleUserDisconnected(String sessionId, String websocketSessionId) {
        log.info("User disconnected from session {}", sessionId);
        
        Optional<SessionConnection> connectionOpt = connectionRepository.findByWebsocketSessionId(websocketSessionId);
        if (connectionOpt.isPresent()) {
            SessionConnection connection = connectionOpt.get();
            connection.setStatus(SessionConnection.ConnectionStatus.DISCONNECTED);
            connection.setLastActivity(LocalDateTime.now());
            connectionRepository.save(connection);
            
            // Notify services about user disconnection
            notifyServiceUserLeft(sessionId, connection.getUserId(), "chat-service");
            notifyServiceUserLeft(sessionId, connection.getUserId(), "crdt-service");
        }
    }

    public void handleMessage(String sessionId, String websocketSessionId, Map<String, Object> messageData) {
        String messageType = (String) messageData.get("type");
        String targetService = (String) messageData.get("targetService");
        
        log.debug("Handling message type: {} for service: {}", messageType, targetService);
        
        switch (targetService) {
            case "chat":
                handleChatMessage(sessionId, websocketSessionId, messageData);
                break;
            case "crdt":
                handleCRDTMessage(sessionId, websocketSessionId, messageData);
                break;
            case "heartbeat":
                handleHeartbeat(websocketSessionId);
                break;
            default:
                log.warn("Unknown target service: {}", targetService);
        }
        
        // Update last activity
        updateLastActivity(websocketSessionId);
    }

    private void handleChatMessage(String sessionId, String websocketSessionId, Map<String, Object> messageData) {
        try {
            // Forward message to chat service
            chatServiceClient.sendMessage(sessionId, messageData);
            
            // Log communication
            logServiceCommunication(sessionId, "connection-service", "chat-service", 
                    ServiceCommunication.MessageType.CHAT_MESSAGE, messageData);
        } catch (Exception e) {
            log.error("Error forwarding chat message", e);
        }
    }

    private void handleCRDTMessage(String sessionId, String websocketSessionId, Map<String, Object> messageData) {
        try {
            // Forward message to CRDT service
            crdtServiceClient.sendOperation(sessionId, messageData);
            
            // Log communication
            logServiceCommunication(sessionId, "connection-service", "crdt-service", 
                    ServiceCommunication.MessageType.CRDT_OPERATION, messageData);
        } catch (Exception e) {
            log.error("Error forwarding CRDT message", e);
        }
    }

    private void handleHeartbeat(String websocketSessionId) {
        connectionRepository.updateHeartbeat(websocketSessionId, LocalDateTime.now());
    }

    public void handleConnectionError(String sessionId, String websocketSessionId, String error) {
        log.error("Connection error for session {}: {}", sessionId, error);
        
        Optional<SessionConnection> connectionOpt = connectionRepository.findByWebsocketSessionId(websocketSessionId);
        if (connectionOpt.isPresent()) {
            SessionConnection connection = connectionOpt.get();
            connection.setStatus(SessionConnection.ConnectionStatus.RECONNECTING);
            connectionRepository.save(connection);
        }
    }

    private void notifyServiceUserJoined(String sessionId, String userId, String service) {
        Map<String, Object> notification = Map.of(
                "sessionId", sessionId,
                "userId", userId,
                "action", "user_joined"
        );
        
        logServiceCommunication(sessionId, "connection-service", service, 
                ServiceCommunication.MessageType.USER_JOIN, notification);
    }

    private void notifyServiceUserLeft(String sessionId, String userId, String service) {
        Map<String, Object> notification = Map.of(
                "sessionId", sessionId,
                "userId", userId,
                "action", "user_left"
        );
        
        logServiceCommunication(sessionId, "connection-service", service, 
                ServiceCommunication.MessageType.USER_LEAVE, notification);
    }

    private void updateLastActivity(String websocketSessionId) {
        connectionRepository.updateHeartbeat(websocketSessionId, LocalDateTime.now());
    }

    private void logServiceCommunication(String sessionId, String sourceService, String targetService,
                                         ServiceCommunication.MessageType messageType, Map<String, Object> content) {
        ServiceCommunication communication = new ServiceCommunication();
        communication.setSessionId(sessionId);
        communication.setSourceService(sourceService);
        communication.setTargetService(targetService);
        communication.setMessageType(messageType);
        communication.setMessageContent(content);
        communication.setStatus(ServiceCommunication.CommunicationStatus.COMPLETED);
        communication.setProcessedAt(LocalDateTime.now());
        
        communicationRepository.save(communication);
    }

    public List<SessionConnection> getActiveConnections(String sessionId) {
        return connectionRepository.findActiveConnectionsBySession(sessionId);
    }

    public void cleanupStaleConnections() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(5);
        List<SessionConnection> staleConnections = connectionRepository.findStaleConnections(staleThreshold);
        
        for (SessionConnection connection : staleConnections) {
            connection.setStatus(SessionConnection.ConnectionStatus.DISCONNECTED);
            connectionRepository.save(connection);
        }
    }
}
