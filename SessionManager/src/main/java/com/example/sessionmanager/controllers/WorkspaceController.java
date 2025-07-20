package com.example.sessionmanager.controllers;

import com.example.sessionmanager.dto.CreateWorkspaceRequest;
import com.example.sessionmanager.dto.UserDTO;
import com.example.sessionmanager.dto.WorkspaceInviteRequest;
import com.example.sessionmanager.entities.Workspace;
import com.example.sessionmanager.entities.WorkspaceInvitation;
import com.example.sessionmanager.entities.WorkspacePermission;
import com.example.sessionmanager.services.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

     private final WorkspaceService workspaceService;

     private UUID getCurrentUserId() {
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          if (authentication != null && authentication.getPrincipal() instanceof UserDTO) {
               UserDTO user = (UserDTO) authentication.getPrincipal();
               return user.getUserId();
          }
          throw new RuntimeException("User not authenticated");
     }

     @PostMapping
     public ResponseEntity<Workspace> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
          UUID userId = getCurrentUserId();
          log.info("Creating workspace: {} for user: {}", request.getName(), userId);
          Workspace workspace = workspaceService.createWorkspace(request, userId);
          return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
     }

     @GetMapping("/{wsId}")
     public ResponseEntity<Workspace> getWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Getting workspace: {} for user: {}", wsId, userId);

          // Check if user has access to this workspace
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.VIEWER)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          Workspace workspace = workspaceService.getWorkspace(wsId);
          return ResponseEntity.ok(workspace);
     }

     @PutMapping("/{wsId}")
     public ResponseEntity<Workspace> updateWorkspace(
               @PathVariable UUID wsId,
               @Valid @RequestBody CreateWorkspaceRequest request) {
          UUID userId = getCurrentUserId();
          log.info("Updating workspace: {} for user: {}", wsId, userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          Workspace workspace = workspaceService.updateWorkspace(wsId, request, userId);
          return ResponseEntity.ok(workspace);
     }

     @DeleteMapping("/{wsId}")
     public ResponseEntity<Void> deleteWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Deleting workspace: {} for user: {}", wsId, userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.deleteWorkspace(wsId, userId);
          return ResponseEntity.noContent().build();
     }

     @PostMapping("/{wsId}/archive")
     public ResponseEntity<Void> archiveWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Archiving workspace: {} for user: {}", wsId, userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.archiveWorkspace(wsId, userId);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/my-workspaces")
     public ResponseEntity<List<Workspace>> getUserWorkspaces() {
          UUID userId = getCurrentUserId();
          log.info("Getting workspaces for user: {}", userId);
          List<Workspace> workspaces = workspaceService.getUserWorkspaces(userId);
          return ResponseEntity.ok(workspaces);
     }

     @GetMapping("/owned")
     public ResponseEntity<List<Workspace>> getOwnedWorkspaces() {
          UUID userId = getCurrentUserId();
          log.info("Getting owned workspaces for user: {}", userId);
          List<Workspace> workspaces = workspaceService.getOwnedWorkspaces(userId);
          return ResponseEntity.ok(workspaces);
     }

     @GetMapping("/public")
     public ResponseEntity<List<Workspace>> getPublicWorkspaces() {
          log.info("Getting public workspaces");
          List<Workspace> workspaces = workspaceService.getPublicWorkspaces();
          return ResponseEntity.ok(workspaces);
     }

     @PostMapping("/invite")
     public ResponseEntity<List<WorkspaceInvitation>> inviteUsers(@Valid @RequestBody WorkspaceInviteRequest request) {
          UUID userId = getCurrentUserId();
          log.info("Inviting users to workspace: {} by user: {}", request.getWsId(), userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(request.getWsId(), userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          List<WorkspaceInvitation> invitations = workspaceService.inviteUsers(request, userId);
          return ResponseEntity.ok(invitations);
     }

     @PostMapping("/invitations/{invitationId}/accept")
     public ResponseEntity<Void> acceptInvitation(@PathVariable UUID invitationId) {
          UUID userId = getCurrentUserId();
          log.info("Accepting invitation: {} by user: {}", invitationId, userId);
          workspaceService.acceptInvitation(invitationId, userId);
          return ResponseEntity.ok().build();
     }

     @PostMapping("/invitations/{invitationId}/reject")
     public ResponseEntity<Void> rejectInvitation(@PathVariable UUID invitationId) {
          UUID userId = getCurrentUserId();
          log.info("Rejecting invitation: {} by user: {}", invitationId, userId);
          workspaceService.rejectInvitation(invitationId, userId);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/invitations")
     public ResponseEntity<List<WorkspaceInvitation>> getUserInvitations() {
          UUID userId = getCurrentUserId();
          log.info("Getting invitations for user: {}", userId);
          List<WorkspaceInvitation> invitations = workspaceService.getUserInvitations(userId);
          return ResponseEntity.ok(invitations);
     }

     @GetMapping("/{wsId}/permissions")
     public ResponseEntity<List<WorkspacePermission>> getWorkspacePermissions(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Getting permissions for workspace: {} by user: {}", wsId, userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          List<WorkspacePermission> permissions = workspaceService.getWorkspacePermissions(wsId);
          return ResponseEntity.ok(permissions);
     }

     @DeleteMapping("/{wsId}/users/{targetUserId}")
     public ResponseEntity<Void> removeUserFromWorkspace(
               @PathVariable UUID wsId,
               @PathVariable UUID targetUserId) {
          UUID userId = getCurrentUserId();
          log.info("Removing user: {} from workspace: {} by user: {}", targetUserId, wsId, userId);

          // Check if user has admin access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.removeUserFromWorkspace(wsId, targetUserId, userId);
          return ResponseEntity.ok().build();
     }

     @PostMapping("/{wsId}/deploy")
     public ResponseEntity<Map<String, String>> deployWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Deploying workspace: {} by user: {}", wsId, userId);

          // Check if user has editor access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.EDITOR)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.deployWorkspace(wsId, userId);

          Map<String, String> response = new HashMap<>();
          response.put("message", "Workspace deployment initiated");
          response.put("status", "deploying");
          return ResponseEntity.ok(response);
     }

     @PostMapping("/{wsId}/stop")
     public ResponseEntity<Map<String, String>> stopWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Stopping workspace: {} by user: {}", wsId, userId);

          // Check if user has editor access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.EDITOR)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.stopWorkspace(wsId, userId);

          Map<String, String> response = new HashMap<>();
          response.put("message", "Workspace stopped");
          response.put("status", "stopped");
          return ResponseEntity.ok(response);
     }

     @PostMapping("/{wsId}/restart")
     public ResponseEntity<Map<String, String>> restartWorkspace(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Restarting workspace: {} by user: {}", wsId, userId);

          // Check if user has editor access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.EDITOR)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          workspaceService.restartWorkspace(wsId, userId);

          Map<String, String> response = new HashMap<>();
          response.put("message", "Workspace restarted");
          response.put("status", "running");
          return ResponseEntity.ok(response);
     }

     @GetMapping("/{wsId}/status")
     public ResponseEntity<Map<String, String>> getWorkspaceStatus(@PathVariable UUID wsId) {
          UUID userId = getCurrentUserId();
          log.info("Getting status for workspace: {} by user: {}", wsId, userId);

          // Check if user has viewer access
          if (!workspaceService.hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.VIEWER)) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          String status = workspaceService.getWorkspaceStatus(wsId);

          Map<String, String> response = new HashMap<>();
          response.put("wsId", wsId.toString());
          response.put("status", status);
          return ResponseEntity.ok(response);
     }
}