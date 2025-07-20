package com.example.sessionmanager.services;

import com.example.sessionmanager.client.AuthenticationClient;
import com.example.sessionmanager.dto.CreateWorkspaceRequest;
import com.example.sessionmanager.dto.UserDTO;
import com.example.sessionmanager.dto.WorkspaceInviteRequest;
import com.example.sessionmanager.entities.*;
import com.example.sessionmanager.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

     private final WorkspaceRepository workspaceRepository;
     private final WorkspacePermissionRepository permissionRepository;
     private final WorkspaceInvitationRepository invitationRepository;
     private final UserSessionRepository sessionRepository;
     private final AuthenticationClient authenticationClient;

     @Override
     @Transactional
     public Workspace createWorkspace(CreateWorkspaceRequest request, UUID ownerId) {
          log.info("Creating workspace: {} for owner: {}", request.getName(), ownerId);

          // Check if workspace name already exists for this owner
          if (workspaceRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
               throw new RuntimeException("Workspace with name '" + request.getName() + "' already exists");
          }

          // Create workspace
          Workspace workspace = Workspace.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .ownerId(ownerId)
                    .isPublic(request.getIsPublic())
                    .isTemplate(request.getIsTemplate())
                    .status(Workspace.WorkspaceStatus.ACTIVE)
                    .build();

          workspace = workspaceRepository.save(workspace);

          // Create admin permission for owner
          WorkspacePermission ownerPermission = WorkspacePermission.builder()
                    .wsId(workspace.getWsId())
                    .userId(ownerId)
                    .role(WorkspacePermission.WorkspaceRole.ADMIN)
                    .grantedBy(ownerId)
                    .status(WorkspacePermission.PermissionStatus.ACCEPTED)
                    .build();

          permissionRepository.save(ownerPermission);

          log.info("Workspace created successfully: {}", workspace.getWsId());
          return workspace;
     }

     @Override
     public Workspace getWorkspace(UUID wsId) {
          return workspaceRepository.findByWsIdAndStatus(wsId, Workspace.WorkspaceStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Workspace not found: " + wsId));
     }

     @Override
     @Transactional
     public Workspace updateWorkspace(UUID wsId, CreateWorkspaceRequest request, UUID userId) {
          Workspace workspace = getWorkspace(wsId);

          // Check if user is owner or admin
          if (!workspace.getOwnerId().equals(userId) &&
                    !hasPermission(wsId, userId, WorkspacePermission.WorkspaceRole.ADMIN)) {
               throw new RuntimeException("Insufficient permissions to update workspace");
          }

          workspace.setName(request.getName());
          workspace.setDescription(request.getDescription());
          workspace.setIsPublic(request.getIsPublic());
          workspace.setIsTemplate(request.getIsTemplate());

          return workspaceRepository.save(workspace);
     }

     @Override
     @Transactional
     public void deleteWorkspace(UUID wsId, UUID userId) {
          Workspace workspace = getWorkspace(wsId);

          // Only owner can delete workspace
          if (!workspace.getOwnerId().equals(userId)) {
               throw new RuntimeException("Only workspace owner can delete workspace");
          }

          workspace.setStatus(Workspace.WorkspaceStatus.DELETED);
          workspaceRepository.save(workspace);

          log.info("Workspace deleted: {}", wsId);
     }

     @Override
     @Transactional
     public void archiveWorkspace(UUID wsId, UUID userId) {
          Workspace workspace = getWorkspace(wsId);

          // Only owner can archive workspace
          if (!workspace.getOwnerId().equals(userId)) {
               throw new RuntimeException("Only workspace owner can archive workspace");
          }

          workspace.setStatus(Workspace.WorkspaceStatus.ARCHIVED);
          workspaceRepository.save(workspace);

          log.info("Workspace archived: {}", wsId);
     }

     @Override
     public List<Workspace> getUserWorkspaces(UUID userId) {
          return workspaceRepository.findWorkspacesByUserAccess(userId, Workspace.WorkspaceStatus.ACTIVE);
     }

     @Override
     public List<Workspace> getOwnedWorkspaces(UUID ownerId) {
          return workspaceRepository.findByOwnerIdAndStatus(ownerId, Workspace.WorkspaceStatus.ACTIVE);
     }

     @Override
     public List<Workspace> getPublicWorkspaces() {
          return workspaceRepository.findPublicWorkspaces(Workspace.WorkspaceStatus.ACTIVE);
     }

     @Override
     public boolean hasPermission(UUID wsId, UUID userId, WorkspacePermission.WorkspaceRole requiredRole) {
          // Check if user is workspace owner
          Workspace workspace = workspaceRepository.findById(wsId).orElse(null);
          if (workspace != null && workspace.getOwnerId().equals(userId)) {
               return true; // Owner has all permissions
          }

          // Check user's permission level
          WorkspacePermission.WorkspaceRole userRole = getUserRole(wsId, userId);
          if (userRole == null) {
               return false;
          }

          // Check role hierarchy: ADMIN > EDITOR > VIEWER
          return switch (requiredRole) {
               case VIEWER -> userRole == WorkspacePermission.WorkspaceRole.VIEWER ||
                         userRole == WorkspacePermission.WorkspaceRole.EDITOR ||
                         userRole == WorkspacePermission.WorkspaceRole.ADMIN;
               case EDITOR -> userRole == WorkspacePermission.WorkspaceRole.EDITOR ||
                         userRole == WorkspacePermission.WorkspaceRole.ADMIN;
               case ADMIN -> userRole == WorkspacePermission.WorkspaceRole.ADMIN;
          };
     }

     @Override
     public WorkspacePermission.WorkspaceRole getUserRole(UUID wsId, UUID userId) {
          return permissionRepository.findByWsIdAndUserIdAndStatus(
                    wsId, userId, WorkspacePermission.PermissionStatus.ACCEPTED)
                    .map(WorkspacePermission::getRole)
                    .orElse(null);
     }

     @Override
     public List<WorkspacePermission> getWorkspacePermissions(UUID wsId) {
          return permissionRepository.findByWsIdAndStatus(wsId, WorkspacePermission.PermissionStatus.ACCEPTED);
     }

     @Override
     @Transactional
     public List<WorkspaceInvitation> inviteUsers(WorkspaceInviteRequest request, UUID invitedBy) {
          Workspace workspace = getWorkspace(request.getWsId());
          List<WorkspaceInvitation> invitations = new ArrayList<>();

          // Invite users by ID
          if (request.getUserIds() != null) {
               for (UUID userId : request.getUserIds()) {
                    // Check if user already has permission
                    if (permissionRepository.existsByWsIdAndUserId(request.getWsId(), userId)) {
                         log.warn("User {} already has permission for workspace {}", userId, request.getWsId());
                         continue;
                    }

                    WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                              .workspace(workspace)
                              .invitedBy(invitedBy)
                              .invitedUserId(userId)
                              .role(request.getRole())
                              .status(WorkspaceInvitation.InvitationStatus.PENDING)
                              .build();

                    invitations.add(invitationRepository.save(invitation));
               }
          }

          // Invite users by email
          if (request.getEmails() != null) {
               for (String email : request.getEmails()) {
                    // Check if email already invited
                    if (invitationRepository.existsByWorkspaceWsIdAndInvitedEmail(request.getWsId(), email)) {
                         log.warn("Email {} already invited to workspace {}", email, request.getWsId());
                         continue;
                    }

                    WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                              .workspace(workspace)
                              .invitedBy(invitedBy)
                              .invitedEmail(email)
                              .role(request.getRole())
                              .status(WorkspaceInvitation.InvitationStatus.PENDING)
                              .build();

                    invitations.add(invitationRepository.save(invitation));
               }
          }

          log.info("Created {} invitations for workspace {}", invitations.size(), request.getWsId());
          return invitations;
     }

     @Override
     @Transactional
     public void acceptInvitation(UUID invitationId, UUID userId) {
          WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

          // Verify invitation is for this user
          if (!userId.equals(invitation.getInvitedUserId())) {
               throw new RuntimeException("Invitation not for this user");
          }

          // Check if invitation is still valid
          if (invitation.getStatus() != WorkspaceInvitation.InvitationStatus.PENDING) {
               throw new RuntimeException("Invitation is no longer pending");
          }

          if (invitation.isExpired()) {
               throw new RuntimeException("Invitation has expired");
          }

          // Create workspace permission
          WorkspacePermission permission = WorkspacePermission.builder()
                    .wsId(invitation.getWorkspace().getWsId())
                    .userId(userId)
                    .role(invitation.getRole())
                    .grantedBy(invitation.getInvitedBy())
                    .status(WorkspacePermission.PermissionStatus.ACCEPTED)
                    .build();

          permissionRepository.save(permission);

          // Update invitation status
          invitation.setStatus(WorkspaceInvitation.InvitationStatus.ACCEPTED);
          invitationRepository.save(invitation);

          log.info("User {} accepted invitation {} for workspace {}",
                    userId, invitationId, invitation.getWorkspace().getWsId());
     }

     @Override
     @Transactional
     public void rejectInvitation(UUID invitationId, UUID userId) {
          WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

          // Verify invitation is for this user
          if (!userId.equals(invitation.getInvitedUserId())) {
               throw new RuntimeException("Invitation not for this user");
          }

          invitation.setStatus(WorkspaceInvitation.InvitationStatus.REJECTED);
          invitationRepository.save(invitation);

          log.info("User {} rejected invitation {} for workspace {}",
                    userId, invitationId, invitation.getWorkspace().getWsId());
     }

     @Override
     public List<WorkspaceInvitation> getUserInvitations(UUID userId) {
          // Get user info to find email
          try {
               UserDTO user = authenticationClient.getUserInfo("Bearer " + "dummy-token").getBody();
               String email = user != null ? user.getEmail() : null;

               return invitationRepository.findByUserIdOrEmail(userId, email);
          } catch (Exception e) {
               // Fallback to just user ID
               return invitationRepository.findByInvitedUserId(userId);
          }
     }

     @Override
     @Transactional
     public void removeUserFromWorkspace(UUID wsId, UUID userId, UUID removedBy) {
          Workspace workspace = getWorkspace(wsId);

          // Cannot remove workspace owner
          if (workspace.getOwnerId().equals(userId)) {
               throw new RuntimeException("Cannot remove workspace owner");
          }

          // Remove permission
          permissionRepository.deletePermission(wsId, userId);

          // End user sessions in this workspace
          sessionRepository.endUserSessionsInWorkspace(userId, wsId, LocalDateTime.now());

          log.info("User {} removed from workspace {} by {}", userId, wsId, removedBy);
     }

     @Override
     public void deployWorkspace(UUID wsId, UUID userId) {
          // TODO: Implement deployment logic
          // This would typically call a deployment service
          log.info("Deploying workspace {} by user {}", wsId, userId);
     }

     @Override
     public void stopWorkspace(UUID wsId, UUID userId) {
          // TODO: Implement stop logic
          // This would typically call a deployment service
          log.info("Stopping workspace {} by user {}", wsId, userId);
     }

     @Override
     public void restartWorkspace(UUID wsId, UUID userId) {
          // TODO: Implement restart logic
          // This would typically call a deployment service
          log.info("Restarting workspace {} by user {}", wsId, userId);
     }

     @Override
     public String getWorkspaceStatus(UUID wsId) {
          // TODO: Implement status check logic
          // This would typically call a deployment service
          return "running"; // Placeholder
     }
}