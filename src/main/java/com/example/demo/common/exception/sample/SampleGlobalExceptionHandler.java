package com.example.demo.common.exception.sample;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class SampleGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        List<SampleApiResult.FieldErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

//        String message = errors.isEmpty()
//                ? getMessage(SampleExceptionCode.INVALID_REQUEST.getMessageKey(), null, Locale.getDefault())
//                : errors.get(0).message();

        String message = errors.size() == 1
                ? errors.get(0).message()
                : getMessage(SampleExceptionCode.INVALID_REQUEST.getMessageKey(), null, locale);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(SampleApiResult.fail(
                        SampleExceptionCode.INVALID_REQUEST.getCode(),
                        message,
                        errors
                ));
    }

    @ExceptionHandler(SampleBusinessException.class)
    public ResponseEntity<SampleApiResult<Void>> handleBusinessException(
            SampleBusinessException ex,
            Locale locale
    ) {
        SampleExceptionCode SampleExceptionCode = ex.getExceptionCode();

        String message = getMessage(
                SampleExceptionCode.getMessageKey(),
                null,
                locale
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(SampleApiResult.fail(
                        SampleExceptionCode.getCode(),
                        message
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<SampleApiResult<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            Locale locale
    ) {
        String message = getMessage(
                SampleExceptionCode.DUPLICATE_KEY.getMessageKey(),
                null,
                locale
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(SampleApiResult.fail(
                        SampleExceptionCode.DUPLICATE_KEY.getCode(),
                        message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SampleApiResult<Void>> handleException(
            Exception ex,
            Locale locale
    ) {
        String message = getMessage(
                SampleExceptionCode.SYSTEM_ERROR.getMessageKey(),
                null,
                locale
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SampleApiResult.fail(
                        SampleExceptionCode.SYSTEM_ERROR.getCode(),
                        message
                ));
    }

    private SampleApiResult.FieldErrorResponse toFieldErrorResponse(FieldError error) {
        return new SampleApiResult.FieldErrorResponse(
                error.getField(),
                error.getDefaultMessage()
        );
    }

    private String getMessage(String messageKey, Object[] args, Locale locale) {
        return messageSource.getMessage(
                messageKey,
                args,
                messageKey,
                locale
        );
    }
}