package com.example.sessionmanager.controllers;

import com.example.sessionmanager.dto.AddParticipantRequest;
import com.example.sessionmanager.dto.CreateSessionRequest;
import com.example.sessionmanager.dto.SessionResponse;
import com.example.sessionmanager.dto.UpdateParticipantRoleRequest;
import com.example.sessionmanager.dto.UserDTO;
import com.example.sessionmanager.entities.SessionParticipant;
import com.example.sessionmanager.services.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

     private final SessionService sessionService;

     /**
      * Helper method to extract user ID from authentication
      */
     private UUID getUserIdFromAuthentication(Authentication authentication) {
          if (authentication.getPrincipal() instanceof UserDTO) {
               UserDTO userDTO = (UserDTO) authentication.getPrincipal();
               return userDTO.getUserId();
          }
          throw new IllegalArgumentException("Invalid authentication principal");
     }

     @PostMapping("/sessions")
     public ResponseEntity<SessionResponse> createSession(
               @RequestBody CreateSessionRequest request,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.createSession(request, userId);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }

     @GetMapping("/sessions/{sessionId}")
     public ResponseEntity<SessionResponse> getSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.getSession(sessionId, userId);
          return ResponseEntity.ok(response);
     }

     @PutMapping("/sessions/{sessionId}")
     public ResponseEntity<SessionResponse> updateSession(
               @PathVariable UUID sessionId,
               @RequestBody CreateSessionRequest request,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.updateSession(sessionId, request, userId);
          return ResponseEntity.ok(response);
     }

     @DeleteMapping("/sessions/{sessionId}")
     public ResponseEntity<Void> deleteSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.deleteSession(sessionId, userId);
          return ResponseEntity.noContent().build();
     }

     @PostMapping("/sessions/{sessionId}/archive")
     public ResponseEntity<Void> archiveSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.archiveSession(sessionId, userId);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/sessions/my-sessions")
     public ResponseEntity<List<SessionResponse>> getUserSessions(Authentication authentication) {
          UUID userId = getUserIdFromAuthentication(authentication);
          List<SessionResponse> sessions = sessionService.getUserSessions(userId);
          return ResponseEntity.ok(sessions);
     }

     @GetMapping("/sessions/owned")
     public ResponseEntity<List<SessionResponse>> getOwnedSessions(Authentication authentication) {
          UUID userId = getUserIdFromAuthentication(authentication);
          List<SessionResponse> sessions = sessionService.getOwnedSessions(userId);
          return ResponseEntity.ok(sessions);
     }

     @GetMapping("/sessions/active-deployments")
     public ResponseEntity<List<SessionResponse>> getActiveDeployments() {
          List<SessionResponse> deployments = sessionService.getActiveDeployments();
          return ResponseEntity.ok(deployments);
     }

     @PostMapping("/sessions/{sessionId}/participants")
     public ResponseEntity<Void> addParticipant(
               @PathVariable UUID sessionId,
               @RequestBody AddParticipantRequest request,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.addParticipant(sessionId, request.getEmail(), request.getRole(), userId);
          return ResponseEntity.ok().build();
     }

     @PutMapping("/sessions/{sessionId}/participants/{participantId}/role")
     public ResponseEntity<Void> updateParticipantRole(
               @PathVariable UUID sessionId,
               @PathVariable UUID participantId,
               @RequestBody UpdateParticipantRoleRequest request,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.updateParticipantRole(sessionId, participantId, request.getNewRole(), userId);
          return ResponseEntity.ok().build();
     }

     @DeleteMapping("/sessions/{sessionId}/participants/{participantId}")
     public ResponseEntity<Void> removeParticipant(
               @PathVariable UUID sessionId,
               @PathVariable UUID participantId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.removeParticipant(sessionId, participantId, userId);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/sessions/{sessionId}/participants")
     public ResponseEntity<List<SessionResponse.ParticipantInfo>> getSessionParticipants(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          List<SessionResponse.ParticipantInfo> participants = sessionService.getSessionParticipants(sessionId, userId);
          return ResponseEntity.ok(participants);
     }

     @PostMapping("/sessions/{sessionId}/deploy")
     public ResponseEntity<SessionResponse> deploySession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.deploySession(sessionId, userId);
          return ResponseEntity.ok(response);
     }

     @PostMapping("/sessions/{sessionId}/join")
     public ResponseEntity<SessionResponse> joinDeployedSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.joinDeployedSession(sessionId, userId);
          return ResponseEntity.ok(response);
     }

     @PostMapping("/sessions/{sessionId}/stop")
     public ResponseEntity<Void> stopSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.stopSession(sessionId, userId);
          return ResponseEntity.ok().build();
     }

     @PostMapping("/sessions/{sessionId}/restart")
     public ResponseEntity<Void> restartSession(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          sessionService.restartSession(sessionId, userId);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/sessions/{sessionId}/status")
     public ResponseEntity<SessionResponse> getSessionStatus(
               @PathVariable UUID sessionId,
               Authentication authentication) {

          UUID userId = getUserIdFromAuthentication(authentication);
          SessionResponse response = sessionService.getSessionStatus(sessionId, userId);
          return ResponseEntity.ok(response);
     }
}