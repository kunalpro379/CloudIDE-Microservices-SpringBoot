package com.example.authentication.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PermissionId.class)
public class Permission {

     @Id
     @Column(name = "ws_id", nullable = false)
     private UUID wsId;

     @Id
     @Column(name = "user_id", nullable = false)
     private UUID userId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", insertable = false, updatable = false)
     private User user;

     @Column(name = "role", nullable = false)
     @Enumerated(EnumType.STRING)
     private PermissionRole role;

     @Column(name = "granted_by")
     private UUID grantedBy;

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

     public enum PermissionRole {
          OWNER, WRITE, READ
     }
}