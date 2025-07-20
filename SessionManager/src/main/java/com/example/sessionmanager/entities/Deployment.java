package com.example.sessionmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deployments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deployment {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "deployment_id", updatable = false, nullable = false)
     private UUID deploymentId;

     @Column(name = "session_id", nullable = false)
     private UUID sessionId;

     @Column(name = "deployment_url")
     private String deploymentUrl;

     @Column(name = "container_id")
     private String containerId;

     @Column(name = "port")
     private Integer port;

     @Column(name = "status")
     @Enumerated(EnumType.STRING)
     private DeploymentStatus status = DeploymentStatus.CREATED;

     @Column(name = "owner_id", nullable = false)
     private UUID ownerId;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "started_at")
     private LocalDateTime startedAt;

     @Column(name = "stopped_at")
     private LocalDateTime stoppedAt;

     @Column(name = "last_active")
     private LocalDateTime lastActive;

     @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<DeploymentParticipant> participants;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          lastActive = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          lastActive = LocalDateTime.now();
     }

     public enum DeploymentStatus {
          CREATED, STARTING, RUNNING, STOPPING, STOPPED, FAILED
     }
}