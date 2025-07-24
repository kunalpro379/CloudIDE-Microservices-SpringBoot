package com.example.connectionservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "service_communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCommunication {
    
    @Id
    private String communicationId;
    
    @Indexed
    @Field("session_id")
    private String sessionId;
    
    @Field("source_service")
    private String sourceService;
    
    @Field("target_service")
    private String targetService;
    
    @Field("message_type")
    private MessageType messageType;
    
    @Field("message_content")
    private Map<String, Object> messageContent;
    
    @Field("status")
    private CommunicationStatus status = CommunicationStatus.PENDING;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("processed_at")
    private LocalDateTime processedAt;
    
    @Field("retry_count")
    private Integer retryCount = 0;
    
    public enum MessageType {
        CHAT_MESSAGE,
        CRDT_OPERATION,
        USER_JOIN,
        USER_LEAVE,
        PERMISSION_UPDATE,
        HEARTBEAT,
        ERROR_NOTIFICATION
    }
    
    public enum CommunicationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        RETRY
    }
}
