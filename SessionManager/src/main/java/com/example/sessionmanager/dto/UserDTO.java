package com.example.sessionmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
     private UUID userId;
     private String username;
     private String email;
     private String firstName;
     private String lastName;
     private String profileImageUrl;
     private Boolean emailVerified;
     private Boolean isActive;
     private String oauthProvider;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
     private LocalDateTime lastLogin;
}