package com.threadly.user.exception;

public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(String message, String error) {
        super(message, 500, error);
    }
}
