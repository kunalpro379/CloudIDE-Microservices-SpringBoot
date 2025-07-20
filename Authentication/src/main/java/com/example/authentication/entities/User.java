package com.example.authentication.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

     @Id
     @GeneratedValue(generator = "UUID")
     @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
     @Column(name = "user_id", updatable = false, nullable = false)
     private UUID userId;

     @Column(name = "email", unique = true, nullable = false)
     private String email;

     @Column(name = "password_hash")
     private String passwordHash;

     @Column(name = "username", unique = true, nullable = false)
     private String username;

     @Column(name = "first_name")
     private String firstName;

     @Column(name = "last_name")
     private String lastName;

     @Column(name = "profile_image_url")
     private String profileImageUrl;

     @Column(name = "email_verified")
     private Boolean emailVerified = false;

     @Column(name = "is_active")
     private Boolean isActive = true;

     @Column(name = "oauth_provider")
     private String oauthProvider; // 'google', 'github', 'local'

     @Column(name = "oauth_provider_id")
     private String oauthProviderId;

     @Column(name = "created_at")
     private LocalDateTime createdAt;

     @Column(name = "updated_at")
     private LocalDateTime updatedAt;

     @Column(name = "last_login")
     private LocalDateTime lastLogin;

     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<Permission> permissions;

     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private List<RefreshToken> refreshTokens;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          updatedAt = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
     }

     // UserDetails implementation
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
          return List.of(new SimpleGrantedAuthority("ROLE_USER"));
     }

     @Override
     public String getPassword() {
          return passwordHash;
     }

     @Override
     public String getUsername() {
          return username;
     }

     @Override
     public boolean isAccountNonExpired() {
          return true;
     }

     @Override
     public boolean isAccountNonLocked() {
          return isActive;
     }

     @Override
     public boolean isCredentialsNonExpired() {
          return true;
     }

     @Override
     public boolean isEnabled() {
          return isActive; // Removed emailVerified requirement for development
     }

     // Helper methods
     public boolean isOAuthUser() {
          return oauthProvider != null && !oauthProvider.equals("local");
     }

     public String getFullName() {
          if (firstName != null && lastName != null) {
               return firstName + " " + lastName;
          }
          return username;
     }
}