package com.threadly.user.exception;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String message, String error) {
        super(message, 409, error);
    }
}
