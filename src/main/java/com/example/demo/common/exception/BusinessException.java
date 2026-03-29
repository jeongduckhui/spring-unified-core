package com.example.demo.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionCode code;

    public BusinessException(ExceptionCode code) {
        this.code = code;
    }

    public BusinessException(ExceptionCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}