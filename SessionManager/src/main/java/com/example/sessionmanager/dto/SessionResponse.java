package com.example.sessionmanager.dto;

import com.example.sessionmanager.entities.Deployment;
import com.example.sessionmanager.entities.Session;
import com.example.sessionmanager.entities.SessionParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

     private UUID sessionId;
     private String name;
     private String description;
     private String language;
     private String framework;
     private Boolean isDynamic;
     private UUID ownerId;
     private Session.SessionStatus status;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
     private LocalDateTime lastActive;
     private List<ParticipantInfo> participants;
     private DeploymentInfo deploymentInfo;

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ParticipantInfo {
          private UUID participantId;
          private UUID userId;
          private String email;
          private SessionParticipant.ParticipantRole role;
          private LocalDateTime joinedAt;
          private LocalDateTime lastActive;
          private Boolean isActive;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class DeploymentInfo {
          private UUID deploymentId;
          private String deploymentUrl;
          private String containerId;
          private Integer port;
          private Deployment.DeploymentStatus status;
          private LocalDateTime createdAt;
          private LocalDateTime startedAt;
          private LocalDateTime lastActive;
     }
}