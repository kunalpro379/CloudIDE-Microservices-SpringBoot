package com.example.codeeditorservice.exception;

public class UnauthorizedUserException extends RuntimeException {
    private final int errorCode = 401;

    public UnauthorizedUserException(String msg) {
        super(msg);
    }

    public int getErrorCode() {
        return errorCode;
    }
}
