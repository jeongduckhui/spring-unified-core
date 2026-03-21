package com.example.demo.auth.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;

/**
 * RefreshToken이 서버에서 폐기(revoke)된 경우 발생하는 예외
 *
 * 이 예외는 RefreshToken이 존재하고 만료되지도 않았지만
 * 서버 로직에 의해 "사용 불가능한 상태"로 표시된 경우 발생한다.
 *
 * revoke 상태가 되는 대표적인 상황
 *
 * 사용자가 로그아웃한 경우
 * 특정 디바이스 세션이 강제 로그아웃된 경우
 * RefreshToken Rotation 과정에서 이전 토큰이 폐기된 경우
 * 보안 정책에 의해 세션이 강제 종료된 경우
 *
 * RefreshToken 상태 종류 예
 *
 * 정상 토큰
 * revoked 토큰
 * expired 토큰
 *
 * revoked 토큰은 아직 만료되지 않았더라도
 * 서버 정책상 더 이상 사용할 수 없는 토큰이다.
 *
 * RefreshTokenExpiredException과의 차이
 *
 * RefreshTokenExpiredException
 * 토큰의 만료 시간이 지나서 자동으로 무효화된 상태
 *
 * RefreshTokenRevokedException
 * 서버 로직에 의해 명시적으로 사용 금지된 상태
 *
 * 예시 흐름
 *
 * 사용자 로그인
 * refreshToken A 발급
 *
 * 사용자 로그아웃
 * refreshToken A revoked 처리
 *
 * 이후 클라이언트가 refresh 요청
 *
 * refreshToken A 사용 시도
 *
 * 서버
 * revoked 상태 확인
 *
 * RefreshTokenRevokedException 발생
 *
 * 결과
 *
 * 클라이언트는 다시 로그인해야 한다.
 *
 * 이 예외는 RuntimeException을 상속한다.
 *
 * 이유
 *
 * 인증 및 보안 관련 예외는 대부분 서비스 레이어에서
 * 즉시 처리되기 때문에 checked exception으로 강제할 필요가 없다.
 */
public class RefreshTokenRevokedException extends BusinessException {

    /**
     * 기본 생성자
     *
     * RuntimeException 부모 클래스에
     * 기본 예외 메시지를 전달한다.
     *
     * 이 메시지는 다음과 같은 곳에서 사용될 수 있다.
     *
     * 서버 로그 기록
     * 보안 이벤트 추적
     * GlobalExceptionHandler 응답 메시지
     */
    public RefreshTokenRevokedException() {

        /**
         * RuntimeException 생성자 호출
         *
         * 기본 메시지
         *
         * "Refresh token revoked"
         *
         * 의미
         *
         * 이 토큰은 서버 정책에 의해 이미 폐기되었으며
         * 더 이상 사용할 수 없다.
         */
        super(ExceptionCode.REFRESH_TOKEN_REVOKED);
    }
}