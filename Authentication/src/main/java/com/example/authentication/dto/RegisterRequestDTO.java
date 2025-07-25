package com.example.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDTO {

     @NotBlank(message = "Username is required")
     @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
     private String username;

     @NotBlank(message = "Email is required")
     @Email(message = "Email should be valid")
     private String email;

     @NotBlank(message = "Password is required")
     @Size(min = 6, message = "Password must be at least 6 characters")
     private String password;

     @Size(max = 100, message = "First name must be less than 100 characters")
     private String firstName;

     @Size(max = 100, message = "Last name must be less than 100 characters")
     private String lastName;

     private String profileImageUrl;
}