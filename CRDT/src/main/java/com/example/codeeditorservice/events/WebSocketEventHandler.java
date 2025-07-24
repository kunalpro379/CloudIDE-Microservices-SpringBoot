package com.example.codeeditorservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class WebSocketEventHandler extends TextWebSocketHandler {
    // Stores active WebSocket sessions
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    // Stores document contents as plain text
    private final Map<String, String> documentContents = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new session
        activeSessions.put(session.getId(), session);
    }

    @Override
    @SuppressWarnings("unchecked") // raw Map conversion
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Parse the incoming message
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String messageType = (String) payload.get("type");
        switch (messageType) {
            case "JOIN":
                handleJoinMessage(session, payload);
                break;
            case "LOCAL_EDIT":
                handleLocalEdit(payload);
                break;
            case "FORMATTING":
                handleFormatting(payload);
                break;
        }
    }

    private void handleJoinMessage(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String docId = (String) payload.get("documentId");
        // Create or retrieve the document content store
        String finalDocId = (docId == null) ? "default" : docId;
        String content = documentContents.computeIfAbsent(finalDocId, k -> "");
        // Prepare response with document content and user list
        Map<String, Object> response = Map.of(
                "type", "DOCUMENT_INIT",
                "documentId", finalDocId,
                "content", content,
                "users", getUsernames());
        // Send initialization message
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        // Broadcast user list update to all connected clients
        broadcastUserListUpdate();
    }

    private void handleLocalEdit(Map<String, Object> payload) throws IOException {
        String documentId = (String) payload.get("documentId");
        String content = (String) payload.get("content");
        String username = (String) payload.get("username");
        // Update stored content and broadcast to all clients with sender info
        documentContents.put(documentId, content);
        broadcastDocumentUpdate(documentId, content, username);
    }

    private void handleFormatting(Map<String, Object> payload) throws IOException {
        String documentId = (String) payload.get("documentId");
        String formatting = (String) payload.get("formatting");
        String selectedText = (String) payload.get("selectedText");
        String username = (String) payload.get("username");
        // Retrieve document
        String document = documentContents.get(documentId);
        if (document == null)
            return;
        // Apply formatting (this would need more complex implementation in a real CRDT)
        // For now, this is a placeholder
        Map<String, Object> formattingResponse = Map.of(
                "type", "FORMATTING_UPDATE",
                "documentId", documentId,
                "username", username,
                "formatting", formatting,
                "selectedText", selectedText);
        broadcastToAllClients(formattingResponse);
    }

    private void broadcastDocumentUpdate(String documentId, String content, String username) throws IOException {
        Map<String, Object> updateMessage = Map.of(
                "type", "REMOTE_EDIT",
                "documentId", documentId,
                "content", content,
                "username", username);
        broadcastToAllClients(updateMessage);
    }

    private void broadcastUserListUpdate() throws IOException {
        Map<String, Object> userListUpdate = Map.of(
                "type", "USER_LIST_UPDATE",
                "users", getUsernames());
        broadcastToAllClients(userListUpdate);
    }

    private void broadcastToAllClients(Map<String, Object> message) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        for (WebSocketSession clientSession : activeSessions.values()) {
            clientSession.sendMessage(new TextMessage(jsonMessage));
        }
    }

    private List<String> getUsernames() {
        // In a real implementation, you'd track usernames more robustly
        return activeSessions.values().stream()
                .map(WebSocketSession::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status)
            throws Exception {
        // Remove the closed session
        activeSessions.remove(session.getId());
        // Broadcast updated user list
        broadcastUserListUpdate();
    }
}
