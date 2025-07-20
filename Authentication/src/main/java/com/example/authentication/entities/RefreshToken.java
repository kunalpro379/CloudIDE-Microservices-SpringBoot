package com.example.authentication.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "token_id", updatable = false, nullable = false)
     private UUID tokenId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

     @Column(name = "token_hash", nullable = false)
     private String tokenHash;

     @Column(name = "expires_at", nullable = false)
     private LocalDateTime expiresAt;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "revoked_at")
     private LocalDateTime revokedAt;

     @Column(name = "device_info")
     private String deviceInfo;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
     }

     public boolean isExpired() {
          return LocalDateTime.now().isAfter(expiresAt);
     }

     public boolean isRevoked() {
          return revokedAt != null;
     }

     public boolean isValid() {
          return !isExpired() && !isRevoked();
     }

     public void revoke() {
          revokedAt = LocalDateTime.now();
     }
}