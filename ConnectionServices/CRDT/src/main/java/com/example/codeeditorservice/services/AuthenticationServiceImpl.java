package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.AuthenticationResponseDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.entities.RefreshToken;
import com.example.codeeditorservice.exception.UnauthorizedUserException;
import com.example.codeeditorservice.exception.UserAlreadyExistsException;
import com.example.codeeditorservice.exception.UserNotFoundException;
import com.example.codeeditorservice.repository.UserRepository;
import com.example.codeeditorservice.repository.RefreshTokenRepository;
import com.example.codeeditorservice.Security.SecurityUtil;
import com.example.codeeditorservice.mapper.RegisterRequestDTOUserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RegisterRequestDTOUserMapper registerMapper;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with username " + request.getUsername() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .oauthProvider("local")
                .emailVerified(false)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token
        jwtService.storeRefreshToken(refreshToken, user, extractDeviceInfo(null));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes
                .user(convertUserToDTO(user))
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        log.info("Authenticating user: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            throw new UnauthorizedUserException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token
        jwtService.storeRefreshToken(refreshToken, user, extractDeviceInfo(null));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User authenticated successfully: {}", user.getEmail());

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes
                .user(convertUserToDTO(user))
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> refreshToken(String refreshToken) {
        log.info("Refreshing token");

        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new UnauthorizedUserException("Invalid refresh token");
            }

            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (!jwtService.validateRefreshToken(refreshToken, user)) {
                throw new UnauthorizedUserException("Invalid or expired refresh token");
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            // Revoke old refresh token
            jwtService.revokeRefreshToken(refreshToken);

            // Store new refresh token
            jwtService.storeRefreshToken(newRefreshToken, user, extractDeviceInfo(null));

            log.info("Token refreshed successfully for user: {}", user.getEmail());

            return jwtService.createTokenResponse(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new UnauthorizedUserException("Token refresh failed");
        }
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            jwtService.revokeRefreshToken(token);
            log.info("User logged out successfully");
        }
    }

    @Override
    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        jwtService.revokeAllRefreshTokensForUser(user);
        log.info("All sessions revoked for user: {}", username);
    }

    @Override
    @Transactional
    public AuthenticationResponseDTO authenticateOAuth(String provider, String code, String state) {
        log.info("Authenticating OAuth user with provider: {}", provider);

        // This is a simplified OAuth implementation
        // In a real application, you would:
        // 1. Validate the state parameter
        // 2. Exchange code for access token with the provider
        // 3. Get user info from the provider
        // 4. Create or update user in your database

        // For now, returning a placeholder response
        throw new UnsupportedOperationException("OAuth authentication not fully implemented");
    }

    @Override
    public String getOAuthAuthorizationUrl(String provider, String redirectUri) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Unknown OAuth provider: " + provider);
        }

        // Build authorization URL
        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();
        String clientId = clientRegistration.getClientId();
        String scope = String.join(" ", clientRegistration.getScopes());
        String state = UUID.randomUUID().toString();

        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                authorizationUri, clientId, redirectUri, scope, state);
    }

    @Override
    public void verifyEmail(String token) {
        // Email verification logic
        throw new UnsupportedOperationException("Email verification not implemented");
    }

    @Override
    public void requestPasswordReset(String email) {
        // Password reset request logic
        throw new UnsupportedOperationException("Password reset not implemented");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Password reset logic
        throw new UnsupportedOperationException("Password reset not implemented");
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    @Override
    @Transactional
    public void updateUserProfile(User user) {
        User existingUser = getCurrentUser();

        // Update allowed fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setProfileImageUrl(user.getProfileImageUrl());

        userRepository.save(existingUser);
        log.info("User profile updated: {}", existingUser.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedUserException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens to force re-authentication
        jwtService.revokeAllRefreshTokensForUser(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);
            return user != null && jwtService.isTokenValid(token, user);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        jwtService.revokeRefreshToken(token);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        jwtService.cleanupExpiredTokens();
        log.info("Expired tokens cleaned up");
    }

    // Helper methods
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        if (request == null) {
            return "Unknown Device";
        }

        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        return String.format("Device: %s, IP: %s", userAgent, remoteAddr);
    }

    private Map<String, Object> convertUserToDTO(User user) {
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", user.getUserId());
        userDto.put("username", user.getUsername());
        userDto.put("email", user.getEmail());
        userDto.put("firstName", user.getFirstName());
        userDto.put("lastName", user.getLastName());
        userDto.put("profileImageUrl", user.getProfileImageUrl());
        userDto.put("emailVerified", user.getEmailVerified());
        userDto.put("isActive", user.getIsActive());
        userDto.put("oauthProvider", user.getOauthProvider());
        userDto.put("createdAt", user.getCreatedAt());
        userDto.put("lastLogin", user.getLastLogin());
        return userDto;
    }
}