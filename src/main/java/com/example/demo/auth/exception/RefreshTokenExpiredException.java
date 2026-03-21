package com.example.demo.auth.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;

/**
 * RefreshToken 만료 예외
 *
 * 이 예외는 RefreshToken이 이미 만료된 상태에서
 * 토큰 재발급(refresh)을 시도할 때 발생하는 예외이다.
 *
 * 사용되는 주요 상황
 *
 * 클라이언트가 AccessToken 재발급을 위해
 * /auth/refresh API를 호출했지만
 * 서버에 저장된 RefreshToken이 이미 만료된 경우
 *
 * 예시 흐름
 *
 * AccessToken 만료
 *        ↓
 * 클라이언트 /auth/refresh 호출
 *        ↓
 * 서버에서 RefreshToken 조회
 *        ↓
 * RefreshToken expiresAt 검사
 *        ↓
 * 만료된 경우 이 예외 발생
 *
 * 결과
 *
 * 클라이언트는 다시 로그인해야 한다.
 *
 * 이 예외는 RuntimeException을 상속하므로
 * checked exception 처리 없이
 * 비즈니스 로직에서 바로 throw 할 수 있다.
 *
 * 일반적으로는 GlobalExceptionHandler에서
 * 다음과 같은 응답으로 변환된다.
 *
 * HTTP 401 Unauthorized
 *
 * {
 *   "error": "refresh_token_expired"
 * }
 */
public class RefreshTokenExpiredException extends BusinessException {

    /**
     * 기본 생성자
     *
     * 예외 메시지를 부모 클래스(RuntimeException)에 전달한다.
     *
     * 이 메시지는 로그 기록 또는
     * GlobalExceptionHandler에서 사용할 수 있다.
     */
    public RefreshTokenExpiredException() {

        /**
         * RuntimeException 생성자 호출
         *
         * "Refresh token expired"
         * 라는 기본 예외 메시지를 설정한다.
         */
        super(ExceptionCode.REFRESH_TOKEN_EXPIRED);
    }
}