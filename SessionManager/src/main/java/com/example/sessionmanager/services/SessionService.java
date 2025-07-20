package com.example.sessionmanager.services;

import com.example.sessionmanager.dto.CreateSessionRequest;
import com.example.sessionmanager.dto.SessionResponse;
import com.example.sessionmanager.entities.SessionParticipant;

import java.util.List;
import java.util.UUID;

public interface SessionService {

     // Session CRUD operations
     SessionResponse createSession(CreateSessionRequest request, UUID ownerId);

     SessionResponse getSession(UUID sessionId, UUID userId);

     SessionResponse updateSession(UUID sessionId, CreateSessionRequest request, UUID userId);

     void deleteSession(UUID sessionId, UUID userId);

     void archiveSession(UUID sessionId, UUID userId);

     // Session access and permissions
     List<SessionResponse> getUserSessions(UUID userId);

     List<SessionResponse> getOwnedSessions(UUID ownerId);

     List<SessionResponse> getActiveDeployments();

     // Participant management
     void addParticipant(UUID sessionId, String email, SessionParticipant.ParticipantRole role, UUID addedBy);

     void updateParticipantRole(UUID sessionId, UUID participantId, SessionParticipant.ParticipantRole newRole,
               UUID updatedBy);

     void removeParticipant(UUID sessionId, UUID participantId, UUID removedBy);

     List<SessionResponse.ParticipantInfo> getSessionParticipants(UUID sessionId, UUID userId);

     // Deployment operations
     SessionResponse deploySession(UUID sessionId, UUID userId);

     SessionResponse joinDeployedSession(UUID sessionId, UUID userId);

     void stopSession(UUID sessionId, UUID userId);

     void restartSession(UUID sessionId, UUID userId);

     SessionResponse getSessionStatus(UUID sessionId, UUID userId);

     // Utility methods
     boolean hasPermission(UUID sessionId, UUID userId, SessionParticipant.ParticipantRole requiredRole);

     SessionParticipant.ParticipantRole getUserRole(UUID sessionId, UUID userId);

     boolean isUserOwner(UUID sessionId, UUID userId);

     boolean canUserCreateSession(UUID userId);
}