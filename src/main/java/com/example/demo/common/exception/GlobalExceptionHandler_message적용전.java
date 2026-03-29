package com.example.demo.common.exception;

import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.example.demo.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler_message적용전 {

    private final MessageService messageService;

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResult<?>> handleAuthException(AuthException e) {

        ExceptionCode code = e.getCode();

        String message = messageService.getMessage(
                code.getMessageId(),
                code.getActionType()
        );

        log.warn("AuthException: code={}, message={}",
                code.getCode(), message);

        int status = switch (code) {
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            default -> 401;
        };

        return ResponseEntity
                .status(status)
                .body(ApiResult.fail(
                        ErrorResponse.of(code.getCode(), message)
                ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e) {

        ExceptionCode code = e.getCode();

        String message = messageService.getMessage(
                code.getMessageId(),
                code.getActionType()
        );

        log.warn("BusinessException: code={}, message={}",
                code.getCode(), message);

        ErrorResponse error = ErrorResponse.builder()
                .code(code.getCode())
                .message(message)
                .build();

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.fail(error));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<?>> handleMaxSize(MaxUploadSizeExceededException e) {

        String message = messageService.getMessage(
                ExceptionCode.FILE_SIZE_EXCEEDED.getMessageId(),
                ExceptionCode.FILE_SIZE_EXCEEDED.getActionType()
        );

        log.warn("File upload size exceeded");

        return ResponseEntity
                .badRequest()
                .body(ApiResult.fail(
                        ErrorResponse.of(
                                ExceptionCode.FILE_SIZE_EXCEEDED.getCode(),
                                message
                        )
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {

        log.error("Unhandled Exception", e);

        String message = messageService.getMessage(
                ExceptionCode.INTERNAL_SERVER_ERROR.getMessageId(),
                ExceptionCode.INTERNAL_SERVER_ERROR.getActionType()
        );

        return ResponseEntity
                .status(500)
                .body(ApiResult.fail(
                        ErrorResponse.of(
                                ExceptionCode.INTERNAL_SERVER_ERROR.getCode(),
                                message
                        )
                ));
    }
}