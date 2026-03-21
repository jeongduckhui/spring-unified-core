package com.example.demo.auth.service;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * RefreshToken 재발급 결과 객체
 *
 * 이 객체는 RefreshToken Rotation이 성공했을 때
 * AuthService에서 Controller로 전달되는 결과 데이터이다.
 *
 * 사용되는 흐름
 *
 * 클라이언트
 * POST /auth/refresh 요청
 *
 * 서버
 * AuthController → AuthService.rotateRefreshToken()
 *
 * 기존 RefreshToken 검증
 * RefreshToken Rotation 수행
 * 새로운 AccessToken 발급 준비
 *
 * 이후 Controller에서 이 객체를 사용하여
 *
 * 새로운 AccessToken 생성
 * 새로운 RefreshToken 쿠키 설정
 *
 * 을 수행한다.
 *
 * record를 사용하는 이유
 *
 * record는 Java에서 불변 데이터 전달 객체(Immutable DTO)를
 * 간결하게 만들기 위해 사용하는 구조이다.
 *
 * 특징
 *
 * 모든 필드는 final
 * 자동 getter 생성
 * equals / hashCode / toString 자동 생성
 *
 * 따라서 단순 데이터 전달 객체에는 매우 적합하다.
 */
public record RefreshResult(

        /**
         * 사용자 ID
         *
         * RefreshToken 검증이 완료된 후
         * 해당 토큰이 속한 사용자 ID를 반환한다.
         *
         * 이 값은 이후 AccessToken 생성 시 사용된다.
         *
         * 예
         *
         * JWT payload
         *
         * {
         *   "sub": userId
         * }
         */
        Long userId,

        /**
         * 사용자 권한 목록
         *
         * AccessToken 생성 시 포함되는 권한 정보이다.
         *
         * Spring Security에서는 권한을
         * GrantedAuthority 타입으로 표현한다.
         *
         * 예
         *
         * ROLE_USER
         * ROLE_ADMIN
         *
         * AccessToken 생성 시 이 권한들이 JWT payload에 들어간다.
         */
        Collection<? extends GrantedAuthority> authorities,

        /**
         * 새로 발급된 RefreshToken
         *
         * RefreshToken Rotation 과정에서 생성된
         * 새로운 RefreshToken 값이다.
         *
         * 이 값은 Controller에서
         * HttpOnly Cookie로 설정된다.
         *
         * 흐름
         *
         * AuthService.rotateRefreshToken()
         * ↓
         * RefreshResult 반환
         * ↓
         * Controller에서 Cookie 설정
         */
        String newRefreshToken
) {
}