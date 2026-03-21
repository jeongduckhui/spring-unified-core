package com.example.demo.common.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final ExceptionCode code;

    public AuthException(ExceptionCode code) {
        super(code.getMessage());
        this.code = code;
    }
}