package com.example.demo.common.exception.sample;

import lombok.Getter;

import java.util.List;

@Getter
public class SampleApiResult<T> {

    private final boolean success;
    private final T data;
    private final String code;
    private final String message;
    private final List<FieldErrorResponse> errors;

    private SampleApiResult(
            boolean success,
            T data,
            String code,
            String message,
            List<FieldErrorResponse> errors
    ) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public static <T> SampleApiResult<T> success() {
        return new SampleApiResult<>(
                true,
                null,
                null,
                null,
                null
        );
    }

    public static <T> SampleApiResult<T> success(T data) {
        return new SampleApiResult<>(
                true,
                data,
                null,
                null,
                null
        );
    }

    public static <T> SampleApiResult<T> fail(
            String code,
            String message
    ) {
        return new SampleApiResult<>(
                false,
                null,
                code,
                message,
                null
        );
    }

    public static <T> SampleApiResult<T> fail(
            String code,
            String message,
            List<FieldErrorResponse> errors
    ) {
        return new SampleApiResult<>(
                false,
                null,
                code,
                message,
                errors
        );
    }

    public record FieldErrorResponse(
            String field,
            String message
    ) {
    }
}