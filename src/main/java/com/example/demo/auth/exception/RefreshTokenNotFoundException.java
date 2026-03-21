package com.example.demo.auth.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;

/**
 * RefreshToken이 존재하지 않을 때 발생하는 예외
 *
 * 이 예외는 클라이언트 요청에서 RefreshToken을 찾을 수 없을 때 발생한다.
 *
 * 일반적으로 다음 상황에서 발생한다.
 *
 * 요청에 refreshToken 쿠키가 없는 경우
 * 브라우저에서 쿠키가 삭제된 경우
 * 로그아웃 이후 refresh 요청을 시도한 경우
 * RefreshToken이 만료되어 서버에서 이미 삭제된 경우
 *
 * 예시 흐름
 *
 * 클라이언트
 * POST /auth/refresh
 *
 * 서버
 * 쿠키에서 refreshToken 추출 시도
 *
 * refreshToken 없음
 *
 * RefreshTokenNotFoundException 발생
 *
 * 결과
 *
 * 클라이언트는 다시 로그인해야 한다.
 *
 * 보통 GlobalExceptionHandler에서 다음과 같은 응답으로 변환된다.
 *
 * HTTP 401 Unauthorized
 *
 * {
 *   "error": "refresh_token_not_found"
 * }
 *
 * RefreshTokenExpiredException과의 차이
 *
 * RefreshTokenExpiredException
 * 토큰이 존재하지만 만료된 상태
 *
 * RefreshTokenNotFoundException
 * 토큰 자체가 요청에 존재하지 않음
 *
 * 이 예외는 RuntimeException을 상속한다.
 *
 * 이유
 *
 * 인증 관련 예외는 대부분 서비스 로직에서
 * 별도의 checked exception 처리 없이 바로 throw 하는 것이 일반적이기 때문이다.
 */
public class RefreshTokenNotFoundException extends BusinessException {

    /**
     * 기본 생성자
     *
     * 부모 클래스(RuntimeException)에
     * 예외 메시지를 전달한다.
     *
     * 이 메시지는 다음과 같은 곳에서 사용될 수 있다.
     *
     * 서버 로그 기록
     * GlobalExceptionHandler
     * API 응답 메시지
     */
    public RefreshTokenNotFoundException() {

        /**
         * RuntimeException 생성자 호출
         *
         * 기본 예외 메시지 설정
         *
         * "Refresh token not found"
         */
        super(ExceptionCode.REFRESH_TOKEN_NOT_FOUND);
    }
}