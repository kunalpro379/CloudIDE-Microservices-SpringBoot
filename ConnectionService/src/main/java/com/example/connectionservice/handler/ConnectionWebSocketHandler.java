package com.example.connectionservice.handler;

import com.example.connectionservice.service.ConnectionManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectionWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ConnectionManagerService connectionManagerService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session.getUri());
        String userId = extractUserId(session); // Extract from headers or JWT
        
        log.info("WebSocket connection established for session: {}, user: {}", sessionId, userId);
        
        sessionMap.put(session.getId(), session);
        connectionManagerService.handleUserConnected(sessionId, userId, session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = extractSessionId(session.getUri());
        String payload = message.getPayload().toString();
        
        log.debug("Received message for session {}: {}", sessionId, payload);
        
        try {
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            connectionManagerService.handleMessage(sessionId, session.getId(), messageData);
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendErrorMessage(session, "Invalid message format");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        String sessionId = extractSessionId(session.getUri());
        connectionManagerService.handleConnectionError(sessionId, session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = extractSessionId(session.getUri());
        log.info("WebSocket connection closed for session: {}, status: {}", sessionId, closeStatus);
        
        sessionMap.remove(session.getId());
        connectionManagerService.handleUserDisconnected(sessionId, session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractSessionId(URI uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String extractUserId(WebSocketSession session) {
        // Extract user ID from session attributes or JWT token
        // This is a placeholder - implement based on your authentication mechanism
        return (String) session.getAttributes().get("userId");
    }

    public void sendMessageToSession(String websocketSessionId, Object message) {
        WebSocketSession session = sessionMap.get(websocketSessionId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                log.error("Error sending message to session: {}", websocketSessionId, e);
            }
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            Map<String, Object> errorMessage = Map.of(
                "type", "error",
                "message", error
            );
            String jsonMessage = objectMapper.writeValueAsString(errorMessage);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("Error sending error message", e);
        }
    }
}
