package com.example.authentication.controllers;

import com.example.authentication.dto.AuthenticationRequestDTO;
import com.example.authentication.dto.AuthenticationResponseDTO;
import com.example.authentication.dto.RegisterRequestDTO;
import com.example.authentication.entities.User;
import com.example.authentication.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

     private final AuthenticationService authService;

     @PostMapping("/register")
     public ResponseEntity<AuthenticationResponseDTO> register(
               @Valid @RequestBody RegisterRequestDTO request) {
          log.info("Registration request received for user: {}", request.getEmail());
          return ResponseEntity.ok(authService.register(request));
     }

     @PostMapping("/authenticate")
     public ResponseEntity<AuthenticationResponseDTO> authenticate(
               @Valid @RequestBody AuthenticationRequestDTO request) {
          log.info("Authentication request received for user: {}", request.getUsername());
          return ResponseEntity.ok(authService.authenticate(request));
     }

     @PostMapping("/refresh")
     public ResponseEntity<Map<String, Object>> refreshToken(
               @RequestBody Map<String, String> request) {
          log.info("Token refresh request received");
          String refreshToken = request.get("refreshToken");
          return ResponseEntity.ok(authService.refreshToken(refreshToken));
     }

     @PostMapping("/logout")
     public ResponseEntity<Void> logout(
               HttpServletRequest request,
               HttpServletResponse response) {
          log.info("Logout request received");
          authService.logout(request, response);
          return ResponseEntity.ok().build();
     }

     @PostMapping("/logout-all")
     public ResponseEntity<Void> logoutAll(
               @RequestBody Map<String, String> request) {
          String username = request.get("username");
          log.info("Logout all sessions request received for user: {}", username);
          authService.logoutAll(username);
          return ResponseEntity.ok().build();
     }

     @GetMapping("/validate")
     public ResponseEntity<Map<String, Object>> validateToken(
               @RequestHeader("Authorization") String authHeader) {
          log.info("Token validation request received");

          String token = authHeader;
          if (authHeader.startsWith("Bearer ")) {
               token = authHeader.substring(7);
          }

          boolean isValid = authService.isTokenValid(token);

          Map<String, Object> response = new HashMap<>();
          response.put("valid", isValid);

          return ResponseEntity.ok(response);
     }

     @GetMapping("/user-info")
     public ResponseEntity<Map<String, Object>> getUserInfo() {
          log.info("User info request received");

          User user = authService.getCurrentUser();
          Map<String, Object> userInfo = new HashMap<>();
          userInfo.put("userId", user.getUserId());
          userInfo.put("username", user.getUsername());
          userInfo.put("email", user.getEmail());
          userInfo.put("firstName", user.getFirstName());
          userInfo.put("lastName", user.getLastName());
          userInfo.put("profileImageUrl", user.getProfileImageUrl());
          userInfo.put("emailVerified", user.getEmailVerified());
          userInfo.put("isActive", user.getIsActive());

          return ResponseEntity.ok(userInfo);
     }
}