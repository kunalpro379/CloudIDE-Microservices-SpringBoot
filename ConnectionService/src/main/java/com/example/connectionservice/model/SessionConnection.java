package com.example.connectionservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session_connections")
public class SessionConnection {
    
    @Id
    private String id;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("chat_service_url")
    private String chatServiceUrl;
    
    @Field("crdt_service_url")
    private String crdtServiceUrl;
    
    @Field("is_active")
    private Boolean isActive = true;
    
    @Field("connected_users")
    private Set<String> connectedUsers = new HashSet<>();
    
    @Field("chat_enabled")
    private Boolean chatEnabled = true;
    
    @Field("crdt_enabled")
    private Boolean crdtEnabled = true;
    
    @Field("max_connections")
    private Integer maxConnections = 50;
    
    @Field("current_connections")
    private Integer currentConnections = 0;
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("last_activity")
    private LocalDateTime lastActivity = LocalDateTime.now();
    
    @Field("settings")
    private ConnectionSettings settings = new ConnectionSettings();
    
    public SessionConnection(String sessionId) {
        this.sessionId = sessionId;
        this.chatServiceUrl = "http://localhost:8082";
        this.crdtServiceUrl = "http://localhost:8083";
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionSettings {
        private Boolean autoReconnect = true;
        private Integer reconnectInterval = 5000; // milliseconds
        private Integer maxReconnectAttempts = 5;
        private Boolean enableHeartbeat = true;
        private Integer heartbeatInterval = 30000; // milliseconds
        private Boolean enableCompression = false;
        private Integer messageQueueSize = 1000;
    }
    
    public void addUser(String userId) {
        this.connectedUsers.add(userId);
        this.currentConnections = this.connectedUsers.size();
        this.lastActivity = LocalDateTime.now();
    }
    
    public void removeUser(String userId) {
        this.connectedUsers.remove(userId);
        this.currentConnections = this.connectedUsers.size();
        this.lastActivity = LocalDateTime.now();
    }
    
    public boolean isUserConnected(String userId) {
        return this.connectedUsers.contains(userId);
    }
    
    public boolean canAcceptNewConnection() {
        return this.currentConnections < this.maxConnections;
    }
}
