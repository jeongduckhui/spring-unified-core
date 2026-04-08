package com.example.demo.common.exception;

import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.example.demo.message.constants.MessageActionType;
import com.example.demo.message.service.MessageService;
import com.example.demo.transactionlog.context.TransactionLogContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    // =========================
    // Auth
    // =========================
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResult<?>> handleAuthException(AuthException e) {

        ExceptionCode code = e.getCode();

        String message = messageService.getMessage(
                code.getMessageId(),
                code.getActionType()
        );

        log.warn("AuthException: code={}, message={}", code.getCode(), message);

        int status = switch (code) {
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            default -> 401;
        };

        return ResponseEntity
                .status(status)
                .body(ApiResult.fail(ErrorResponse.of(code.getCode(), message)));
    }

    // =========================
    // Business
    // =========================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e) {

        ExceptionCode code = e.getCode();

        String message = messageService.getMessage(
                code.getMessageId(),
                code.getActionType()
        );

        log.warn("BusinessException: code={}, message={}", code.getCode(), message);

        TransactionLogContext.setMessage(code.getCode(), message);
        TransactionLogContext.setError(e.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.fail(
                        ErrorResponse.of(code.getCode(), message)
                ));
    }

    // =========================
    // Validation (DTO)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {

        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);

        String messageId = fieldError.getDefaultMessage();

        String message = messageService.getMessage(
                messageId,
                MessageActionType.VALIDATION
        );

        log.warn("Validation: field={}, message={}", fieldError.getField(), message);

        return ResponseEntity
                .status(ExceptionCode.INVALID_REQUEST.getStatus())
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.INVALID_REQUEST.getCode(), message)
                ));
    }

    // =========================
    // Validation (Bind)
    // =========================
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResult<?>> handleBindException(BindException e) {

        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);

        String messageId = fieldError.getDefaultMessage();

        String message = messageService.getMessage(
                messageId,
                MessageActionType.VALIDATION
        );

        return ResponseEntity
                .status(ExceptionCode.INVALID_REQUEST.getStatus())
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.INVALID_REQUEST.getCode(), message)
                ));
    }

    // =========================
    // Validation (Param)
    // =========================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<?>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {

        ConstraintViolation<?> violation = e.getConstraintViolations()
                .stream()
                .findFirst()
                .orElse(null);

        String messageId = violation != null ? violation.getMessage() : "INVALID_REQUEST";

        String message = messageService.getMessage(
                messageId,
                MessageActionType.VALIDATION
        );

        return ResponseEntity
                .status(ExceptionCode.INVALID_REQUEST.getStatus())
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.INVALID_REQUEST.getCode(), message)
                ));
    }

    // =========================
    // File size
    // =========================
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<?>> handleMaxSize(MaxUploadSizeExceededException e) {

        String message = messageService.getMessage(
                ExceptionCode.FILE_SIZE_EXCEEDED.getMessageId(),
                ExceptionCode.FILE_SIZE_EXCEEDED.getActionType()
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.FILE_SIZE_EXCEEDED.getCode(), message)
                ));
    }

    // =========================
    // 500
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {

        log.error("Unhandled Exception", e);

        String message = messageService.getMessage(
                ExceptionCode.INTERNAL_SERVER_ERROR.getMessageId(),
                ExceptionCode.INTERNAL_SERVER_ERROR.getActionType()
        );

        TransactionLogContext.setMessage(
                ExceptionCode.INTERNAL_SERVER_ERROR.getCode(),
                message
        );
        TransactionLogContext.setError(e.getMessage());

        return ResponseEntity
                .status(500)
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.INTERNAL_SERVER_ERROR.getCode(), message)
                ));
    }
}