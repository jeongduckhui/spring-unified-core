package com.example.demo.auth.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;

/**
 * RefreshToken이 유효하지 않을 때 발생하는 예외
 *
 * 이 예외는 RefreshToken이 존재는 하지만
 * 정상적인 토큰이 아닌 경우 발생한다.
 *
 * 대표적인 상황
 *
 * 토큰 형식이 잘못된 경우
 * 토큰 해시값이 서버 저장 값과 일치하지 않는 경우
 * 변조된 토큰인 경우
 * Rotation 이후 이전 토큰을 다시 사용하는 경우
 *
 * RefreshTokenExpiredException과의 차이
 *
 * RefreshTokenExpiredException
 * 토큰은 정상적인 토큰이지만 만료된 상태
 *
 * RefreshTokenInvalidException
 * 토큰 자체가 정상적인 토큰이 아님
 *
 * 예시 흐름
 *
 * 클라이언트
 * /auth/refresh 호출
 *
 * 서버
 * refreshToken 조회
 *
 * 저장된 tokenHash 비교
 *
 * 일치하지 않음
 *
 * RefreshTokenInvalidException 발생
 *
 * 결과
 *
 * 보통 HTTP 401 Unauthorized 응답으로 처리된다.
 *
 * 이 예외는 RuntimeException을 상속한다.
 *
 * 이유
 *
 * 서비스 로직에서 체크 예외 처리 없이
 * 바로 throw 하기 위함이다.
 */
public class RefreshTokenInvalidException extends BusinessException {

    /**
     * 기본 생성자
     *
     * 부모 클래스(RuntimeException)에
     * 예외 메시지를 전달한다.
     *
     * 이 메시지는
     * 로그 기록
     * GlobalExceptionHandler
     * API 응답 메시지
     *
     * 등에 사용될 수 있다.
     */
    public RefreshTokenInvalidException() {

        /**
         * RuntimeException 생성자 호출
         *
         * 기본 메시지
         *
         * "Invalid refresh token"
         */
        super(ExceptionCode.REFRESH_TOKEN_INVALID);
    }
}