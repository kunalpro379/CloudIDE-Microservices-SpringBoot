package com.example.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

     @GetMapping("/auth")
     @PostMapping("/auth")
     public ResponseEntity<Map<String, Object>> authServiceFallback() {
          log.warn("Authentication service is currently unavailable - fallback triggered");

          Map<String, Object> response = new HashMap<>();
          response.put("error", "Service Unavailable");
          response.put("message", "Authentication service is temporarily unavailable. Please try again later.");
          response.put("service", "authentication-service");
          response.put("timestamp", System.currentTimeMillis());

          return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
     }

     @GetMapping("/session")
     @PostMapping("/session")
     public ResponseEntity<Map<String, Object>> sessionServiceFallback() {
          log.warn("Session service is currently unavailable - fallback triggered");

          Map<String, Object> response = new HashMap<>();
          response.put("error", "Service Unavailable");
          response.put("message", "Session service is temporarily unavailable. Please try again later.");
          response.put("service", "session-manager");
          response.put("timestamp", System.currentTimeMillis());

          return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
     }

     @GetMapping("/general")
     @PostMapping("/general")
     public ResponseEntity<Map<String, Object>> generalFallback() {
          log.warn("General service fallback triggered");

          Map<String, Object> response = new HashMap<>();
          response.put("error", "Service Unavailable");
          response.put("message", "The requested service is temporarily unavailable. Please try again later.");
          response.put("timestamp", System.currentTimeMillis());

          return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
     }
}