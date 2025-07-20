package com.example.authentication.services;

import com.example.authentication.dto.AuthenticationRequestDTO;
import com.example.authentication.dto.AuthenticationResponseDTO;
import com.example.authentication.dto.RegisterRequestDTO;
import com.example.authentication.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AuthenticationService {

     // Basic authentication
     AuthenticationResponseDTO register(RegisterRequestDTO request);

     AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request);

     // Token management
     Map<String, Object> refreshToken(String refreshToken);

     void logout(HttpServletRequest request, HttpServletResponse response);

     void logoutAll(String username);

     // OAuth authentication
     AuthenticationResponseDTO authenticateOAuth(String provider, String code, String state);

     String getOAuthAuthorizationUrl(String provider, String redirectUri);

     // User verification
     void verifyEmail(String token);

     void requestPasswordReset(String email);

     void resetPassword(String token, String newPassword);

     // User management
     User getCurrentUser();

     void updateUserProfile(User user);

     void changePassword(String currentPassword, String newPassword);

     // Utility methods
     boolean isTokenValid(String token);

     void revokeToken(String token);

     void cleanupExpiredTokens();
}