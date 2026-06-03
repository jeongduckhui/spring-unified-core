package com.example.demo.common.exception.sample;

import com.example.demo.common.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class SampleBusinessException extends RuntimeException {

    private final SampleExceptionCode exceptionCode;

    public SampleBusinessException(SampleExceptionCode exceptionCode) {
        super(exceptionCode.getMessageKey());
        this.exceptionCode = exceptionCode;
    }

    public SampleBusinessException(SampleExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public SampleBusinessException(SampleExceptionCode exceptionCode, Throwable cause) {
        super(exceptionCode.getMessageKey(), cause);
        this.exceptionCode = exceptionCode;
    }

    public SampleBusinessException(SampleExceptionCode exceptionCode, String message, Throwable cause) {
        super(message, cause);
        this.exceptionCode = exceptionCode;
    }
}