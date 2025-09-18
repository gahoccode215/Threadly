package com.threadly.user.exception;

public class UserNotFoundException extends BaseException{
    public UserNotFoundException(String message, String error) {
        super(message, 404, error);
    }
}
