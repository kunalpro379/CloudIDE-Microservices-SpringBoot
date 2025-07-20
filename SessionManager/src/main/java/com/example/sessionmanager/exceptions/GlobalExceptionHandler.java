package com.example.sessionmanager.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

     @ExceptionHandler(SessionNotFoundException.class)
     public ResponseEntity<Map<String, Object>> handleSessionNotFoundException(SessionNotFoundException ex) {
          log.error("Session not found: {}", ex.getMessage());

          Map<String, Object> response = new HashMap<>();
          response.put("timestamp", LocalDateTime.now());
          response.put("status", HttpStatus.NOT_FOUND.value());
          response.put("error", "Session Not Found");
          response.put("message", ex.getMessage());

          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
     }

     @ExceptionHandler(UnauthorizedAccessException.class)
     public ResponseEntity<Map<String, Object>> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
          log.error("Unauthorized access: {}", ex.getMessage());

          Map<String, Object> response = new HashMap<>();
          response.put("timestamp", LocalDateTime.now());
          response.put("status", HttpStatus.FORBIDDEN.value());
          response.put("error", "Forbidden");
          response.put("message", ex.getMessage());

          return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
     }

     @ExceptionHandler(UserLimitExceededException.class)
     public ResponseEntity<Map<String, Object>> handleUserLimitExceededException(UserLimitExceededException ex) {
          log.error("User limit exceeded: {}", ex.getMessage());

          Map<String, Object> response = new HashMap<>();
          response.put("timestamp", LocalDateTime.now());
          response.put("status", HttpStatus.BAD_REQUEST.value());
          response.put("error", "User Limit Exceeded");
          response.put("message", ex.getMessage());

          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
     }

     @ExceptionHandler(RuntimeException.class)
     public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
          log.error("Runtime exception: {}", ex.getMessage(), ex);

          Map<String, Object> response = new HashMap<>();
          response.put("timestamp", LocalDateTime.now());
          response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
          response.put("error", "Internal Server Error");
          response.put("message", "An unexpected error occurred");

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
     }

     @ExceptionHandler(Exception.class)
     public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
          log.error("Generic exception: {}", ex.getMessage(), ex);

          Map<String, Object> response = new HashMap<>();
          response.put("timestamp", LocalDateTime.now());
          response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
          response.put("error", "Internal Server Error");
          response.put("message", "An unexpected error occurred");

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
     }
}