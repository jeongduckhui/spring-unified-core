package com.example.demo.auth.exception;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;

/**
 * RefreshToken 재사용 공격(Reuse Attack)이 감지되었을 때 발생하는 예외
 *
 * 이 예외는 RefreshToken Rotation 구조에서
 * 이미 사용된 RefreshToken이 다시 사용될 때 발생한다.
 *
 * RefreshToken Rotation 구조 설명
 *
 * 일반적인 RefreshToken 시스템에서는
 * 하나의 RefreshToken이 여러 번 사용될 수 있다.
 *
 * 그러나 보안이 강화된 시스템에서는
 * RefreshToken Rotation 방식을 사용한다.
 *
 * Rotation 방식에서는
 *
 * refreshToken 사용
 *        ↓
 * 기존 refreshToken 폐기
 *        ↓
 * 새로운 refreshToken 발급
 *
 * 즉
 *
 * 이전 refreshToken은 더 이상 사용할 수 없다.
 *
 * 공격 시나리오 예
 *
 * 정상 사용자
 *
 * refreshToken A 사용
 *        ↓
 * refreshToken B 발급
 *
 * 공격자
 *
 * 탈취한 refreshToken A 사용 시도
 *
 * 이 경우 서버에서는
 *
 * 이미 폐기된 refreshToken이 다시 사용된 것을 감지한다.
 *
 * 이 상황을
 *
 * RefreshToken Reuse Attack
 *
 * 이라고 한다.
 *
 * 이 공격이 감지되면 보통 다음과 같은 조치를 한다.
 *
 * 해당 사용자 모든 RefreshToken revoke
 * 해당 디바이스 세션 종료
 * 재로그인 요구
 *
 * 이 예외는 RuntimeException을 상속한다.
 *
 * 이유
 *
 * 보안 관련 예외는 서비스 로직에서 즉시 처리되어야 하며
 * checked exception으로 강제 처리할 필요가 없기 때문이다.
 */
public class RefreshTokenReuseDetectedException extends BusinessException {

    /**
     * 기본 생성자
     *
     * RuntimeException 부모 클래스에
     * 기본 예외 메시지를 전달한다.
     *
     * 이 메시지는
     *
     * 서버 로그 기록
     * 보안 이벤트 추적
     * GlobalExceptionHandler 응답 생성
     *
     * 등에 활용될 수 있다.
     */
    public RefreshTokenReuseDetectedException() {

        /**
         * RuntimeException 생성자 호출
         *
         * 예외 메시지
         *
         * "Refresh token reuse detected"
         *
         * 이 메시지는 보안 로그에서
         * RefreshToken 탈취 공격 탐지 이벤트를 의미한다.
         */
        super(ExceptionCode.REFRESH_TOKEN_REUSE_DETECTED);
    }
}