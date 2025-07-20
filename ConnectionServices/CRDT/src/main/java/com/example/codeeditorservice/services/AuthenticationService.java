package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.entities.User;
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