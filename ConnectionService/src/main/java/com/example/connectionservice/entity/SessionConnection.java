package com.example.connectionservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Document(collection = "session_connections")
@CompoundIndex(def = "{'session_id': 1, 'user_id': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionConnection {
    
    @Id
    private String connectionId;
    
    @Indexed
    @Field("session_id")
    private String sessionId;
    
    @Indexed
    @Field("user_id")
    private String userId;
    
    @Field("username")
    private String username;
    
    @Field("websocket_session_id")
    private String websocketSessionId;
    
    @Field("connection_status")
    private ConnectionStatus status = ConnectionStatus.CONNECTED;
    
    @Field("services_connected")
    private Set<ServiceType> servicesConnected;
    
    @Field("permissions")
    private Map<String, Object> permissions;
    
    @Field("client_info")
    private Map<String, String> clientInfo;
    
    @CreatedDate
    @Field("connected_at")
    private LocalDateTime connectedAt;
    
    @LastModifiedDate
    @Field("last_activity")
    private LocalDateTime lastActivity;
    
    @Field("last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    public enum ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        RECONNECTING,
        IDLE
    }
    
    public enum ServiceType {
        CHAT,
        CRDT,
        VOICE,
        SCREEN_SHARE
    }
}
