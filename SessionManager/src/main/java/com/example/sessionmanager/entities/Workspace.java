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
@Table(name = "workspaces")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "ws_id", updatable = false, nullable = false)
     private UUID wsId;

     @Column(name = "name", nullable = false)
     private String name;

     @Column(name = "description")
     private String description;

     @Column(name = "owner_id", nullable = false)
     private UUID ownerId;

     @Column(name = "is_public")
     private Boolean isPublic = false;

     @Column(name = "is_template")
     private Boolean isTemplate = false;

     @Column(name = "status")
     @Enumerated(EnumType.STRING)
     private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "updated_at")
     private LocalDateTime updatedAt;

     @Column(name = "last_active")
     private LocalDateTime lastActive;

     @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<WorkspacePermission> permissions;

     @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<WorkspaceInvitation> invitations;

     @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<UserSession> sessions;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          updatedAt = LocalDateTime.now();
          lastActive = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
     }

     public enum WorkspaceStatus {
          ACTIVE, ARCHIVED, DELETED
     }
}