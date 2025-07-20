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
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "session_id", updatable = false, nullable = false)
     private UUID sessionId;

     @Column(name = "user_id", nullable = false)
     private UUID userId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "ws_id", nullable = false)
     private Workspace workspace;

     @Column(name = "status")
     @Enumerated(EnumType.STRING)
     private SessionStatus status = SessionStatus.ACTIVE;

     @Column(name = "last_activity")
     private LocalDateTime lastActivity;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "ended_at")
     private LocalDateTime endedAt;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          lastActivity = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          lastActivity = LocalDateTime.now();
     }

     public void endSession() {
          status = SessionStatus.INACTIVE;
          endedAt = LocalDateTime.now();
     }

     public enum SessionStatus {
          ACTIVE, INACTIVE, DISCONNECTED
     }
}