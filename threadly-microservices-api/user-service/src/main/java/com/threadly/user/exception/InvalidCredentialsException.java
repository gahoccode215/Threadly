package com.threadly.user.exception;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException(String message, String error) {
        super(message, 401, error);
    }
}
