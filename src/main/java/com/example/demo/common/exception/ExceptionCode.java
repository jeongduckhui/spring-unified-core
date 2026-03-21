package com.example.demo.common.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // =========================
    // 공통
    // =========================
    INTERNAL_SERVER_ERROR("E500", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("E400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

    // =========================
    // 인증
    // =========================
    UNAUTHORIZED("E401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // =========================
    // 사용자
    // =========================
    USER_NOT_FOUND("U404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // =========================
    // 토큰
    // =========================
    REFRESH_TOKEN_EXPIRED("R401", "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("R404", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_REVOKED("R403", "리프레시 토큰이 폐기되었습니다.", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_REUSE_DETECTED("R409", "리프레시 토큰 재사용이 감지되었습니다.", HttpStatus.CONFLICT),
    REFRESH_TOKEN_INVALID("R409", "리프레시 토큰이 유효하지 않습니다.", HttpStatus.CONFLICT),

    // =========================
    // 파일
    // =========================
    FILE_EMPTY("F400", "파일이 비어있습니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("F401", "파일 크기를 초과했습니다.", HttpStatus.BAD_REQUEST),
    FILE_INVALID_EXTENSION("F402", "허용되지 않은 파일 확장자입니다.", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND("F404", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_DOWNLOAD_FAILED("F500", "파일 다운로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED("F501", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("F502", "파일 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_PATH_INVALID("F403", "유효하지 않은 파일 경로입니다.", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_ERROR("F503", "파일 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NAME_INVALID("F405", "파일명이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ExceptionCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}