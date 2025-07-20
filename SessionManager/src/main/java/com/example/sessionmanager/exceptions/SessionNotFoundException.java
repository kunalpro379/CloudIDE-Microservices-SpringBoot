package com.example.sessionmanager.exceptions;

public class SessionNotFoundException extends RuntimeException {
    
    public SessionNotFoundException(String message) {
        super(message);
    }
    
    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 