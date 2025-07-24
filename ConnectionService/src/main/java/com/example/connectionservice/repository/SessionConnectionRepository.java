package com.example.connectionservice.repository;

import com.example.connectionservice.entity.SessionConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionConnectionRepository extends MongoRepository<SessionConnection, String> {
    
    Optional<SessionConnection> findBySessionIdAndUserId(String sessionId, String userId);
    
    List<SessionConnection> findBySessionId(String sessionId);
    
    List<SessionConnection> findBySessionIdAndStatus(String sessionId, SessionConnection.ConnectionStatus status);
    
    Optional<SessionConnection> findByWebsocketSessionId(String websocketSessionId);
    
    @Query("{'session_id': ?0, 'status': 'CONNECTED'}")
    List<SessionConnection> findActiveConnectionsBySession(String sessionId);
    
    @Query("{'last_heartbeat': {'$lt': ?0}}")
    List<SessionConnection> findStaleConnections(LocalDateTime staleThreshold);
    
    @Query("{'user_id': ?0}")
    @Update("{'$set': {'status': ?1, 'last_activity': ?2}}")
    void updateConnectionStatus(String userId, SessionConnection.ConnectionStatus status, LocalDateTime lastActivity);
    
    @Query("{'websocket_session_id': ?0}")
    @Update("{'$set': {'last_heartbeat': ?1}}")
    void updateHeartbeat(String websocketSessionId, LocalDateTime heartbeat);
    
    long countBySessionIdAndStatus(String sessionId, SessionConnection.ConnectionStatus status);
    
    @Query("{'session_id': ?0, 'services_connected': ?1}")
    List<SessionConnection> findBySessionIdAndServiceType(String sessionId, SessionConnection.ServiceType serviceType);
}
