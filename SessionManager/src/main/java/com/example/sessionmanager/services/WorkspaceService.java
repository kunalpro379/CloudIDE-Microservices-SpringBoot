package com.example.sessionmanager.services;

import com.example.sessionmanager.dto.CreateWorkspaceRequest;
import com.example.sessionmanager.dto.WorkspaceInviteRequest;
import com.example.sessionmanager.entities.Workspace;
import com.example.sessionmanager.entities.WorkspaceInvitation;
import com.example.sessionmanager.entities.WorkspacePermission;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

     // Workspace CRUD operations
     Workspace createWorkspace(CreateWorkspaceRequest request, UUID ownerId);

     Workspace getWorkspace(UUID wsId);

     Workspace updateWorkspace(UUID wsId, CreateWorkspaceRequest request, UUID userId);

     void deleteWorkspace(UUID wsId, UUID userId);

     void archiveWorkspace(UUID wsId, UUID userId);

     // Workspace access and permissions
     List<Workspace> getUserWorkspaces(UUID userId);

     List<Workspace> getOwnedWorkspaces(UUID ownerId);

     List<Workspace> getPublicWorkspaces();

     // Permission management
     boolean hasPermission(UUID wsId, UUID userId, WorkspacePermission.WorkspaceRole requiredRole);

     WorkspacePermission.WorkspaceRole getUserRole(UUID wsId, UUID userId);

     List<WorkspacePermission> getWorkspacePermissions(UUID wsId);

     // Invitation system
     List<WorkspaceInvitation> inviteUsers(WorkspaceInviteRequest request, UUID invitedBy);

     void acceptInvitation(UUID invitationId, UUID userId);

     void rejectInvitation(UUID invitationId, UUID userId);

     List<WorkspaceInvitation> getUserInvitations(UUID userId);

     void removeUserFromWorkspace(UUID wsId, UUID userId, UUID removedBy);

     // Workspace deployment operations
     void deployWorkspace(UUID wsId, UUID userId);

     void stopWorkspace(UUID wsId, UUID userId);

     void restartWorkspace(UUID wsId, UUID userId);

     String getWorkspaceStatus(UUID wsId);
}