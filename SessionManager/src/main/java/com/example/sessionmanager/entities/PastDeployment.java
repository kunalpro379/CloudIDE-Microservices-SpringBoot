package com.example.sessionmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "past_deployments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastDeployment {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "past_deployment_id", updatable = false, nullable = false)
     private UUID pastDeploymentId;

     @Column(name = "original_deployment_id", nullable = false)
     private UUID originalDeploymentId;

     @Column(name = "session_id", nullable = false)
     private UUID sessionId;

     @Column(name = "deployment_url")
     private String deploymentUrl;

     @Column(name = "container_id")
     private String containerId;

     @Column(name = "port")
     private Integer port;

     @Column(name = "owner_id", nullable = false)
     private UUID ownerId;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "started_at")
     private LocalDateTime startedAt;

     @Column(name = "stopped_at")
     private LocalDateTime stoppedAt;

     @Column(name = "total_runtime_minutes")
     private Long totalRuntimeMinutes;

     @Column(name = "reason_for_stop")
     private String reasonForStop;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
     }
}