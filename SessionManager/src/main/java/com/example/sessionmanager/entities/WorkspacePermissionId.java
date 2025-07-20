package com.example.sessionmanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspacePermissionId implements Serializable {
     private UUID wsId;
     private UUID userId;
}