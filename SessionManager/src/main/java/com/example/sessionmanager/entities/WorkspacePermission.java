package com.example.sessionmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(WorkspacePermissionId.class)
public class WorkspacePermission {

     @Id
     @Column(name = "ws_id", nullable = false)
     private UUID wsId;

     @Id
     @Column(name = "user_id", nullable = false)
     private UUID userId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "ws_id", insertable = false, updatable = false)
     private Workspace workspace;

     @Column(name = "role", nullable = false)
     @Enumerated(EnumType.STRING)
     private WorkspaceRole role;

     @Column(name = "granted_by")
     private UUID grantedBy;

     @Column(name = "status")
     @Enumerated(EnumType.STRING)
     private PermissionStatus status = PermissionStatus.PENDING;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "updated_at")
     private LocalDateTime updatedAt;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          updatedAt = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
     }

     public enum WorkspaceRole {
          ADMIN, EDITOR, VIEWER
     }

     public enum PermissionStatus {
          PENDING, ACCEPTED, REJECTED
     }
}