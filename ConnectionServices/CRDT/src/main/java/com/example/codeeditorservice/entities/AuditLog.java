package com.example.codeeditorservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "log_id")
     private Long logId;

     @Column(name = "ws_id")
     private UUID wsId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User user;

     @Column(name = "action", nullable = false)
     private String action;

     @Column(name = "resource_type")
     private String resourceType;

     @Column(name = "resource_id")
     private String resourceId;

     @JdbcTypeCode(SqlTypes.JSON)
     @Column(name = "old_value", columnDefinition = "jsonb")
     private Map<String, Object> oldValue;

     @JdbcTypeCode(SqlTypes.JSON)
     @Column(name = "new_value", columnDefinition = "jsonb")
     private Map<String, Object> newValue;

     @Column(name = "ip_address")
     private String ipAddress;

     @Column(name = "user_agent")
     private String userAgent;

     @Column(name = "timestamp")
     private LocalDateTime timestamp;

     @PrePersist
     protected void onCreate() {
          timestamp = LocalDateTime.now();
     }
}