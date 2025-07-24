package com.example.connectionservice.repository;

import com.example.connectionservice.entity.ServiceCommunication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceCommunicationRepository extends MongoRepository<ServiceCommunication, String> {
    
    List<ServiceCommunication> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    @Query("{'status': 'PENDING'}")
    List<ServiceCommunication> findPendingCommunications();
    
    @Query("{'status': 'FAILED', 'retry_count': {'$lt': 3}}")
    List<ServiceCommunication> findFailedCommunicationsForRetry();
    
    @Query("{'session_id': ?0, 'message_type': ?1}")
    List<ServiceCommunication> findBySessionIdAndMessageType(String sessionId, ServiceCommunication.MessageType messageType);
    
    @Query("{'source_service': ?0, 'target_service': ?1}")
    List<ServiceCommunication> findBySourceAndTargetService(String sourceService, String targetService);
    
    @Query("{'created_at': {'$gte': ?0, '$lte': ?1}}")
    List<ServiceCommunication> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}
