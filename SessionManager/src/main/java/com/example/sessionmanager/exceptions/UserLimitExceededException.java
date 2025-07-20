package com.example.sessionmanager.exceptions;

public class UserLimitExceededException extends RuntimeException {
    
    public UserLimitExceededException(String message) {
        super(message);
    }
    
    public UserLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
} 