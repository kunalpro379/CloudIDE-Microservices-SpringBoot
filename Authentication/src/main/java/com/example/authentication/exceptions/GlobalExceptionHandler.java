package com.example.authentication.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(UserAlreadyExistsException.class)
     public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
          return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
     }

     @ExceptionHandler(UserNotFoundException.class)
     public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
     }

     @ExceptionHandler(UnauthorizedUserException.class)
     public ResponseEntity<String> handleUnauthorizedUserException(UnauthorizedUserException ex) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
     }

     @ExceptionHandler(RuntimeException.class)
     public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
     }
}