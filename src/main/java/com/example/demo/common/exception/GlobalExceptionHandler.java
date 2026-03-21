package com.example.demo.common.exception;

import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 전역 예외 처리 핸들러
 *
 * 처리 대상:
 * - 인증/인가 예외 (AuthException)
 * - 비즈니스 예외 (BusinessException)
 * - 파일 업로드 크기 초과 예외
 * - 기타 모든 예외 (Exception)
 *
 * 응답 정책:
 * - ApiResult.fail 구조로 통일
 * - 에러코드 기반 응답
 * - 로그 레벨 구분 (warn / error)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 인증/인가 예외 처리
     *
     * 처리 상세:
     * - UNAUTHORIZED -> 401 Unauthorized
     * - FORBIDDEN -> 403 Forbidden
     *
     * @param e AuthException
     * @return ResponseEntity (401 or 403)
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResult<?>> handleAuthException(AuthException e) {

        log.warn("AuthException: code={}, message={}",
                e.getCode().getCode(), e.getCode().getMessage());

        int status = switch (e.getCode()) {
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            default -> 401;
        };

        return ResponseEntity
                .status(status)
                .body(ApiResult.fail(
                        ErrorResponse.of(e.getCode())
                ));
    }

    /**
     * 비즈니스 예외 처리
     *
     * 처리 대상: 유효성 검증 실패, 파일 업로드 오류, 도메인 규칙 위반 등
     *
     * @param e BusinessException
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e) {

        log.warn("BusinessException: code={}, message={}",
                e.getCode().getCode(), e.getCode().getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(e.getCode().getCode())
                .message(e.getCode().getMessage())
                .build();

        return ResponseEntity
                .status(e.getCode().getStatus())
                .body(ApiResult.fail(error));
    }

    /**
     * 파일 업로드 크기 초과 예외 처리
     *
     * 참고: Spring MultipartResolver 단계(컨트롤러 진입 전)에서 발생하므로
     * BusinessException으로 변환되지 않아 별도 처리가 필요함.
     *
     * 응답 정책: 400 Bad Request, FILE_SIZE_EXCEEDED 코드 반환
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<?>> handleMaxSize(MaxUploadSizeExceededException e) {

        log.warn("File upload size exceeded", e);

        return ResponseEntity
                .badRequest()
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.FILE_SIZE_EXCEEDED)
                ));
    }

    /**
     * 최상위 예외 처리
     *
     * 처리 대상: 예측하지 못한 모든 예외 (500 Internal Server Error)
     * 보안을 위해 상세 스택트레이스는 로그에만 남기고 사용자에게는 공통 에러 응답 전달.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {

        log.error("Unhandled Exception", e);

        return ResponseEntity
                .status(500)
                .body(ApiResult.fail(
                        ErrorResponse.of(ExceptionCode.INTERNAL_SERVER_ERROR)
                ));
    }
}