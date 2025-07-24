package com.example.codeeditorservice.exception;
import org.springframework.security.core.AuthenticationException; // 🔴 Note: Not used here

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}