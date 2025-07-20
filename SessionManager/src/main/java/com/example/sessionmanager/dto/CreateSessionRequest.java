package com.example.sessionmanager.dto;

import com.example.sessionmanager.entities.SessionParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

     private String name;
     private String description;
     private String language;
     private String framework;
     private Boolean isDynamic;
     private List<ParticipantInvite> participants;

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ParticipantInvite {
          private String email;
          private SessionParticipant.ParticipantRole role;
          private String message;
     }
}