import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
package com.example.codeeditorservice.Dto;
import com.example.codeeditorservice.entities.Permission;
import com.example.codeeditorservice.entities.User;


import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDocDTO {
    @NotNull(message = "User is required")
    @JsonProperty(required = true)
    private String username;
    @NotNull(message = "Permission is required")
    @JsonProperty(required = true)
    private Permission permission;
}