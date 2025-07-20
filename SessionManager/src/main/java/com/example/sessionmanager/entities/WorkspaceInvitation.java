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
@Table(name = "workspace_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvitation {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "invitation_id", updatable = false, nullable = false)
     private UUID invitationId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "ws_id", nullable = false)
     private Workspace workspace;

     @Column(name = "invited_by", nullable = false)
     private UUID invitedBy;

     @Column(name = "invited_user_id")
     private UUID invitedUserId;

     @Column(name = "invited_email")
     private String invitedEmail;

     @Column(name = "role", nullable = false)
     @Enumerated(EnumType.STRING)
     private WorkspacePermission.WorkspaceRole role;

     @Column(name = "status")
     @Enumerated(EnumType.STRING)
     private InvitationStatus status = InvitationStatus.PENDING;

     @Column(name = "expires_at")
     private LocalDateTime expiresAt;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "updated_at")
     private LocalDateTime updatedAt;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          updatedAt = LocalDateTime.now();
          if (expiresAt == null) {
               expiresAt = LocalDateTime.now().plusDays(7);
          }
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
     }

     public boolean isExpired() {
          return LocalDateTime.now().isAfter(expiresAt);
     }

     public enum InvitationStatus {
          PENDING, ACCEPTED, REJECTED, EXPIRED
     }
}