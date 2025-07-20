package com.example.sessionmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkspaceRequest {

     @NotBlank(message = "Workspace name is required")
     @Size(min = 3, max = 255, message = "Workspace name must be between 3 and 255 characters")
     private String name;

     @Size(max = 1000, message = "Description must be less than 1000 characters")
     private String description;

     private Boolean isPublic = false;

     private Boolean isTemplate = false;
}