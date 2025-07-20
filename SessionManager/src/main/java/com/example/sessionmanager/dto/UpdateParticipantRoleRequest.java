package com.example.sessionmanager.dto;

import com.example.sessionmanager.entities.SessionParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParticipantRoleRequest {
     private SessionParticipant.ParticipantRole newRole;
}