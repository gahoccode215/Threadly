package com.threadly.user.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final int status;
    private final String error;

    protected BaseException(String message, int status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }
}
