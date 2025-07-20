package com.example.sessionmanager.services;

import com.example.sessionmanager.client.AuthenticationClient;
import com.example.sessionmanager.dto.CreateSessionRequest;
import com.example.sessionmanager.dto.SessionResponse;
import com.example.sessionmanager.entities.*;
import com.example.sessionmanager.repositories.*;
import com.example.sessionmanager.exceptions.SessionNotFoundException;
import com.example.sessionmanager.exceptions.UnauthorizedAccessException;
import com.example.sessionmanager.exceptions.UserLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {

     private final SessionRepository sessionRepository;
     private final SessionParticipantRepository sessionParticipantRepository;
     private final SessionInvitationRepository sessionInvitationRepository;
     private final DeploymentRepository deploymentRepository;
     private final PastDeploymentRepository pastDeploymentRepository;
     private final AuthenticationClient authenticationClient;

     private static final int FREE_USER_SESSION_LIMIT = 1;

     @Override
     public SessionResponse createSession(CreateSessionRequest request, UUID ownerId) {
          // Check if user can create more sessions
          if (!canUserCreateSession(ownerId)) {
               throw new UserLimitExceededException(
                         "Free users can only create one session. Please upgrade to Pro plan for more sessions.");
          }

          // Create session
          Session session = Session.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .language(request.getLanguage())
                    .framework(request.getFramework())
                    .isDynamic(request.getIsDynamic())
                    .ownerId(ownerId)
                    .status(Session.SessionStatus.CREATED)
                    .build();

          session = sessionRepository.save(session);

          // Add owner as participant
          SessionParticipant ownerParticipant = SessionParticipant.builder()
                    .session(session)
                    .userId(ownerId)
                    .email(getUserEmail(ownerId))
                    .role(SessionParticipant.ParticipantRole.OWNER)
                    .isActive(true)
                    .build();

          sessionParticipantRepository.save(ownerParticipant);

          // Add other participants if provided
          if (request.getParticipants() != null) {
               for (CreateSessionRequest.ParticipantInvite invite : request.getParticipants()) {
                    SessionInvitation invitation = SessionInvitation.builder()
                              .session(session)
                              .email(invite.getEmail())
                              .invitedBy(ownerId)
                              .role(invite.getRole())
                              .message(invite.getMessage())
                              .status(SessionInvitation.InvitationStatus.PENDING)
                              .build();
                    sessionInvitationRepository.save(invitation);
               }
          }

          log.info("Session created successfully: {} by user: {}", session.getSessionId(), ownerId);
          return mapToSessionResponse(session);
     }

     @Override
     public SessionResponse getSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          // Check if user has access to this session
          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.VIEWER)) {
               throw new UnauthorizedAccessException("User does not have access to this session");
          }

          return mapToSessionResponse(session);
     }

     @Override
     public SessionResponse updateSession(UUID sessionId, CreateSessionRequest request, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!isUserOwner(sessionId, userId)) {
               throw new UnauthorizedAccessException("Only session owner can update session");
          }

          session.setName(request.getName());
          session.setDescription(request.getDescription());
          session.setLanguage(request.getLanguage());
          session.setFramework(request.getFramework());
          session.setIsDynamic(request.getIsDynamic());

          session = sessionRepository.save(session);
          return mapToSessionResponse(session);
     }

     @Override
     public void deleteSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!isUserOwner(sessionId, userId)) {
               throw new UnauthorizedAccessException("Only session owner can delete session");
          }

          // Archive instead of delete
          session.setStatus(Session.SessionStatus.ARCHIVED);
          sessionRepository.save(session);
     }

     @Override
     public void archiveSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!isUserOwner(sessionId, userId)) {
               throw new UnauthorizedAccessException("Only session owner can archive session");
          }

          session.setStatus(Session.SessionStatus.ARCHIVED);
          sessionRepository.save(session);
     }

     @Override
     public List<SessionResponse> getUserSessions(UUID userId) {
          List<Session> sessions = sessionRepository.findSessionsByParticipantId(userId);
          return sessions.stream()
                    .map(this::mapToSessionResponse)
                    .collect(Collectors.toList());
     }

     @Override
     public List<SessionResponse> getOwnedSessions(UUID ownerId) {
          List<Session> sessions = sessionRepository.findByOwnerId(ownerId);
          return sessions.stream()
                    .map(this::mapToSessionResponse)
                    .collect(Collectors.toList());
     }

     @Override
     public List<SessionResponse> getActiveDeployments() {
          List<Session> sessions = sessionRepository.findActiveDeployments();
          return sessions.stream()
                    .map(this::mapToSessionResponse)
                    .collect(Collectors.toList());
     }

     @Override
     public void addParticipant(UUID sessionId, String email, SessionParticipant.ParticipantRole role, UUID addedBy) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, addedBy, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to add participants");
          }

          // Check if participant already exists
          Optional<SessionParticipant> existingParticipant = sessionParticipantRepository
                    .findBySessionSessionIdAndEmail(sessionId, email);

          if (existingParticipant.isPresent()) {
               throw new RuntimeException("Participant already exists in this session");
          }

          // Get user ID from email (you'll need to implement this)
          UUID participantUserId = getUserIdByEmail(email);

          SessionParticipant participant = SessionParticipant.builder()
                    .session(session)
                    .userId(participantUserId)
                    .email(email)
                    .role(role)
                    .isActive(true)
                    .build();

          sessionParticipantRepository.save(participant);
     }

     @Override
     public void updateParticipantRole(UUID sessionId, UUID participantId, SessionParticipant.ParticipantRole newRole,
               UUID updatedBy) {
          if (!hasPermission(sessionId, updatedBy, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to update participant roles");
          }

          SessionParticipant participant = sessionParticipantRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

          if (!participant.getSession().getSessionId().equals(sessionId)) {
               throw new RuntimeException("Participant does not belong to this session");
          }

          participant.setRole(newRole);
          sessionParticipantRepository.save(participant);
     }

     @Override
     public void removeParticipant(UUID sessionId, UUID participantId, UUID removedBy) {
          if (!hasPermission(sessionId, removedBy, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to remove participants");
          }

          SessionParticipant participant = sessionParticipantRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

          if (!participant.getSession().getSessionId().equals(sessionId)) {
               throw new RuntimeException("Participant does not belong to this session");
          }

          if (participant.getRole() == SessionParticipant.ParticipantRole.OWNER) {
               throw new RuntimeException("Cannot remove session owner");
          }

          sessionParticipantRepository.delete(participant);
     }

     @Override
     public List<SessionResponse.ParticipantInfo> getSessionParticipants(UUID sessionId, UUID userId) {
          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.VIEWER)) {
               throw new UnauthorizedAccessException("User does not have access to this session");
          }

          List<SessionParticipant> participants = sessionParticipantRepository.findBySessionSessionId(sessionId);
          return participants.stream()
                    .map(this::mapToParticipantInfo)
                    .collect(Collectors.toList());
     }

     @Override
     public SessionResponse deploySession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to deploy session");
          }

          // Check if session is already deployed
          Optional<Deployment> existingDeployment = deploymentRepository.findActiveDeploymentBySessionId(sessionId);
          if (existingDeployment.isPresent()) {
               // Join existing deployment
               return joinDeployedSession(sessionId, userId);
          }

          // Create new deployment
          Deployment deployment = Deployment.builder()
                    .sessionId(sessionId)
                    .ownerId(userId)
                    .status(Deployment.DeploymentStatus.CREATED)
                    .deploymentUrl("http://localhost:8080/session/" + sessionId) // Mock URL
                    .port(8080) // Mock port
                    .build();

          deployment = deploymentRepository.save(deployment);

          // Update session status
          session.setStatus(Session.SessionStatus.DEPLOYED);
          sessionRepository.save(session);

          log.info("Session deployed successfully: {} by user: {}", sessionId, userId);
          return mapToSessionResponse(session);
     }

     @Override
     public SessionResponse joinDeployedSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.VIEWER)) {
               throw new UnauthorizedAccessException("User does not have access to this session");
          }

          // Check if deployment exists
          Optional<Deployment> deployment = deploymentRepository.findActiveDeploymentBySessionId(sessionId);
          if (deployment.isEmpty()) {
               throw new RuntimeException("Session is not deployed");
          }

          log.info("User joined deployed session: {} by user: {}", sessionId, userId);
          return mapToSessionResponse(session);
     }

     @Override
     public void stopSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to stop session");
          }

          Optional<Deployment> deployment = deploymentRepository.findActiveDeploymentBySessionId(sessionId);
          if (deployment.isPresent()) {
               // Archive deployment to past deployments
               Deployment activeDeployment = deployment.get();
               PastDeployment pastDeployment = PastDeployment.builder()
                         .originalDeploymentId(activeDeployment.getDeploymentId())
                         .sessionId(sessionId)
                         .deploymentUrl(activeDeployment.getDeploymentUrl())
                         .containerId(activeDeployment.getContainerId())
                         .port(activeDeployment.getPort())
                         .ownerId(activeDeployment.getOwnerId())
                         .startedAt(activeDeployment.getStartedAt())
                         .stoppedAt(LocalDateTime.now())
                         .totalRuntimeMinutes(calculateRuntimeMinutes(activeDeployment.getStartedAt()))
                         .reasonForStop("Manual stop by user")
                         .build();

               pastDeploymentRepository.save(pastDeployment);
               deploymentRepository.delete(activeDeployment);
          }

          session.setStatus(Session.SessionStatus.STOPPED);
          sessionRepository.save(session);
     }

     @Override
     public void restartSession(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.EDITOR)) {
               throw new UnauthorizedAccessException("Insufficient permissions to restart session");
          }

          // Create new deployment
          deploySession(sessionId, userId);
     }

     @Override
     public SessionResponse getSessionStatus(UUID sessionId, UUID userId) {
          Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

          if (!hasPermission(sessionId, userId, SessionParticipant.ParticipantRole.VIEWER)) {
               throw new UnauthorizedAccessException("User does not have access to this session");
          }

          return mapToSessionResponse(session);
     }

     @Override
     public boolean hasPermission(UUID sessionId, UUID userId, SessionParticipant.ParticipantRole requiredRole) {
          Optional<SessionParticipant> participant = sessionParticipantRepository
                    .findBySessionSessionIdAndUserId(sessionId, userId);

          if (participant.isEmpty() || !participant.get().getIsActive()) {
               return false;
          }

          SessionParticipant.ParticipantRole userRole = participant.get().getRole();
          return userRole.ordinal() <= requiredRole.ordinal();
     }

     @Override
     public SessionParticipant.ParticipantRole getUserRole(UUID sessionId, UUID userId) {
          Optional<SessionParticipant> participant = sessionParticipantRepository
                    .findBySessionSessionIdAndUserId(sessionId, userId);

          return participant.map(SessionParticipant::getRole)
                    .orElse(null);
     }

     @Override
     public boolean isUserOwner(UUID sessionId, UUID userId) {
          Optional<SessionParticipant> owner = sessionParticipantRepository.findOwnerBySessionId(sessionId);
          return owner.isPresent() && owner.get().getUserId().equals(userId);
     }

     @Override
     public boolean canUserCreateSession(UUID userId) {
          long sessionCount = sessionRepository.countByOwnerId(userId);
          return sessionCount < FREE_USER_SESSION_LIMIT;
     }

     // Helper methods
     private SessionResponse mapToSessionResponse(Session session) {
          List<SessionParticipant> participants = sessionParticipantRepository
                    .findBySessionSessionId(session.getSessionId());
          Optional<Deployment> deployment = deploymentRepository
                    .findActiveDeploymentBySessionId(session.getSessionId());

          return SessionResponse.builder()
                    .sessionId(session.getSessionId())
                    .name(session.getName())
                    .description(session.getDescription())
                    .language(session.getLanguage())
                    .framework(session.getFramework())
                    .isDynamic(session.getIsDynamic())
                    .ownerId(session.getOwnerId())
                    .status(session.getStatus())
                    .createdAt(session.getCreatedAt())
                    .updatedAt(session.getUpdatedAt())
                    .lastActive(session.getLastActive())
                    .participants(participants.stream()
                              .map(this::mapToParticipantInfo)
                              .collect(Collectors.toList()))
                    .deploymentInfo(deployment.map(this::mapToDeploymentInfo).orElse(null))
                    .build();
     }

     private SessionResponse.ParticipantInfo mapToParticipantInfo(SessionParticipant participant) {
          return SessionResponse.ParticipantInfo.builder()
                    .participantId(participant.getParticipantId())
                    .userId(participant.getUserId())
                    .email(participant.getEmail())
                    .role(participant.getRole())
                    .joinedAt(participant.getJoinedAt())
                    .lastActive(participant.getLastActive())
                    .isActive(participant.getIsActive())
                    .build();
     }

     private SessionResponse.DeploymentInfo mapToDeploymentInfo(Deployment deployment) {
          return SessionResponse.DeploymentInfo.builder()
                    .deploymentId(deployment.getDeploymentId())
                    .deploymentUrl(deployment.getDeploymentUrl())
                    .containerId(deployment.getContainerId())
                    .port(deployment.getPort())
                    .status(deployment.getStatus())
                    .createdAt(deployment.getCreatedAt())
                    .startedAt(deployment.getStartedAt())
                    .lastActive(deployment.getLastActive())
                    .build();
     }

         private String getUserEmail(UUID userId) {
        try {
            // This should call the authentication service to get user email
            // For now, return a mock email - in production, call authentication service
            return "user-" + userId + "@example.com";
        } catch (Exception e) {
            log.error("Error getting user email for userId: {}", userId, e);
            return "unknown@example.com";
        }
    }

    private UUID getUserIdByEmail(String email) {
        try {
            // This should call the authentication service to get user ID by email
            // For now, return a mock UUID - in production, call authentication service
            return UUID.randomUUID();
        } catch (Exception e) {
            log.error("Error getting user ID for email: {}", email, e);
            return UUID.randomUUID();
        }
    }

     private Long calculateRuntimeMinutes(LocalDateTime startedAt) {
          if (startedAt == null)
               return 0L;
          return java.time.Duration.between(startedAt, LocalDateTime.now()).toMinutes();
     }
}