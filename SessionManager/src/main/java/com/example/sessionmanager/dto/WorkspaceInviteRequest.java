package com.example.sessionmanager.dto;

import com.example.sessionmanager.entities.WorkspacePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInviteRequest {

     @NotNull(message = "Workspace ID is required")
     private UUID wsId;

     // Either userIds or emails should be provided
     private List<UUID> userIds;

     private List<@Email String> emails;

     @NotNull(message = "Role is required")
     private WorkspacePermission.WorkspaceRole role;

     private String message; // Optional invitation message
}