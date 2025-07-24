package com.example.codeeditorservice.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.codeeditorservice.enums.Permission;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDocDTO {
    @JsonProperty(required = true)
    private String username;
    @JsonProperty(required = true)
    private Permission permission;
}