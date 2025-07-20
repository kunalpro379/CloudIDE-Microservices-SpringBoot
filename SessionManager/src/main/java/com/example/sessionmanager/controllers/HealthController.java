package com.example.sessionmanager.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

     @GetMapping
     public ResponseEntity<Map<String, Object>> health() {
          Map<String, Object> response = new HashMap<>();
          response.put("status", "UP");
          response.put("service", "session-manager");
          response.put("timestamp", LocalDateTime.now());
          response.put("message", "SessionManager service is running");

          return ResponseEntity.ok(response);
     }
}