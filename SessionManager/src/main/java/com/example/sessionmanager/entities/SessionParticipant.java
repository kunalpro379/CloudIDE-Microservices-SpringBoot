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
@Table(name = "session_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionParticipant {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "participant_id", updatable = false, nullable = false)
     private UUID participantId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "session_id", nullable = false)
     private Session session;

     @Column(name = "user_id", nullable = false)
     private UUID userId;

     @Column(name = "email", nullable = false)
     private String email;

     @Column(name = "role", nullable = false)
     @Enumerated(EnumType.STRING)
     private ParticipantRole role = ParticipantRole.VIEWER;

     @Column(name = "joined_at")
     private LocalDateTime joinedAt;

     @Column(name = "last_active")
     private LocalDateTime lastActive;

     @Column(name = "is_active")
     private Boolean isActive = true;

     @PrePersist
     protected void onCreate() {
          joinedAt = LocalDateTime.now();
          lastActive = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          lastActive = LocalDateTime.now();
     }

     public enum ParticipantRole {
          OWNER, EDITOR, VIEWER
     }
}