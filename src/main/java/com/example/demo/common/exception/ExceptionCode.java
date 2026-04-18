package com.example.demo.common.exception;

import com.example.demo.message.constants.MessageActionType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // =========================
    // 공통
    // =========================
    INTERNAL_SERVER_ERROR("E500", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    INVALID_REQUEST("E400", "INVALID_REQUEST", HttpStatus.BAD_REQUEST, MessageActionType.COMMON),

    // =========================
    // 인증
    // =========================
    UNAUTHORIZED("E401", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED, MessageActionType.COMMON),
    FORBIDDEN("E403", "FORBIDDEN", HttpStatus.FORBIDDEN, MessageActionType.COMMON),

    // =========================
    // 사용자
    // =========================
    USER_NOT_FOUND("U404", "USER_NOT_FOUND", HttpStatus.NOT_FOUND, MessageActionType.COMMON),

    // =========================
    // 토큰
    // =========================
    REFRESH_TOKEN_EXPIRED("R401", "REFRESH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, MessageActionType.COMMON),
    REFRESH_TOKEN_NOT_FOUND("R404", "REFRESH_TOKEN_NOT_FOUND", HttpStatus.NOT_FOUND, MessageActionType.COMMON),
    REFRESH_TOKEN_REVOKED("R403", "REFRESH_TOKEN_REVOKED", HttpStatus.FORBIDDEN, MessageActionType.COMMON),
    REFRESH_TOKEN_REUSE_DETECTED("R409", "REFRESH_TOKEN_REUSE_DETECTED", HttpStatus.CONFLICT, MessageActionType.COMMON),
    REFRESH_TOKEN_INVALID("R409", "REFRESH_TOKEN_INVALID", HttpStatus.CONFLICT, MessageActionType.COMMON),

    // =========================
    // 파일
    // =========================
    FILE_EMPTY("F400", "FILE_EMPTY", HttpStatus.BAD_REQUEST, MessageActionType.VALIDATION),
    FILE_SIZE_EXCEEDED("F401", "FILE_SIZE_EXCEEDED", HttpStatus.BAD_REQUEST, MessageActionType.VALIDATION),
    FILE_INVALID_EXTENSION("F402", "FILE_INVALID_EXTENSION", HttpStatus.BAD_REQUEST, MessageActionType.VALIDATION),
    FILE_PATH_INVALID("F403", "FILE_PATH_INVALID", HttpStatus.BAD_REQUEST, MessageActionType.VALIDATION),
    FILE_NOT_FOUND("F404", "FILE_NOT_FOUND", HttpStatus.NOT_FOUND, MessageActionType.COMMON),
    FILE_NAME_INVALID("F405", "FILE_NAME_INVALID", HttpStatus.BAD_REQUEST, MessageActionType.VALIDATION),

    FILE_DOWNLOAD_FAILED("F500", "FILE_DOWNLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    FILE_UPLOAD_FAILED("F501", "FILE_UPLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    FILE_DELETE_FAILED("F502", "FILE_DELETE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    FILE_STORAGE_ERROR("F503", "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    FILE_EXPIRED("F504", "FILE_EXPIRED", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),
    FILE_INVALID_TYPE("F505", "FILE_INVALID_TYPE", HttpStatus.INTERNAL_SERVER_ERROR, MessageActionType.COMMON),

    INVALID_INPUT("S001", "INVALID_INPUT", HttpStatus.BAD_REQUEST, MessageActionType.COMMON);

    private final String code;
    private final String messageId;
    private final HttpStatus status;
    private final String actionType;

    ExceptionCode(String code, String messageId, HttpStatus status, String actionType) {
        this.code = code;
        this.messageId = messageId;
        this.status = status;
        this.actionType = actionType;
    }
}