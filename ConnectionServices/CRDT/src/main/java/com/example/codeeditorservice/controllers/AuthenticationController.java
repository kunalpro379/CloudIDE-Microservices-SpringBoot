package com.example.codeeditorservice.controllers;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8233" })
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Registration request received for email: {}", registerRequestDTO.getEmail());

        AuthenticationResponseDTO response = authenticationService.register(registerRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        log.info("Login request received for username: {}", authenticationRequestDTO.getUsername());

        AuthenticationResponseDTO response = authenticationService.authenticate(authenticationRequestDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestParam("refresh_token") String refreshToken) {
        log.info("Token refresh request received");

        Map<String, Object> response = authenticationService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("Logout request received");

        authenticationService.logout(request, response);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(
            HttpServletRequest request) {
        log.info("Logout all sessions request received");

        // Extract username from current authentication
        String username = extractUsernameFromRequest(request);
        authenticationService.logoutAll(username);

        return ResponseEntity.ok(Map.of("message", "All sessions logged out successfully"));
    }

    @GetMapping("/oauth/{provider}/url")
    public ResponseEntity<Map<String, String>> getOAuthUrl(
            @PathVariable String provider,
            @RequestParam String redirectUri) {
        log.info("OAuth URL request for provider: {}", provider);

        String authUrl = authenticationService.getOAuthAuthorizationUrl(provider, redirectUri);

        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl));
    }

    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<AuthenticationResponseDTO> handleOAuthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("OAuth callback received for provider: {}", provider);

        AuthenticationResponseDTO response = authenticationService.authenticateOAuth(provider, code, state);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String token) {
        log.info("Email verification request received");

        authenticationService.verifyEmail(token);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestParam String email) {
        log.info("Password reset request received for email: {}", email);

        authenticationService.requestPasswordReset(email);

        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Password reset confirmation received");

        authenticationService.resetPassword(token, newPassword);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        log.info("Password change request received");

        authenticationService.changePassword(currentPassword, newPassword);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        log.info("Current user request received");

        var user = authenticationService.getCurrentUser();

        return ResponseEntity.ok(Map.of(
                "user", Map.of(
                        "id", user.getUserId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "profileImageUrl", user.getProfileImageUrl(),
                        "emailVerified", user.getEmailVerified(),
                        "isActive", user.getIsActive(),
                        "oauthProvider", user.getOauthProvider(),
                        "createdAt", user.getCreatedAt(),
                        "lastLogin", user.getLastLogin())));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam String token) {
        log.info("Token validation request received");

        boolean isValid = authenticationService.isTokenValid(token);

        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Token is valid" : "Token is invalid"));
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<Map<String, String>> revokeToken(
            @RequestParam String token) {
        log.info("Token revocation request received");

        authenticationService.revokeToken(token);

        return ResponseEntity.ok(Map.of("message", "Token revoked successfully"));
    }

    // Helper method to extract username from request
    private String extractUsernameFromRequest(HttpServletRequest request) {
        // This would extract username from JWT token in the Authorization header
        // For simplicity, returning empty string - should be implemented properly
        return "";
    }

    // Exception handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Authentication error: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}