package com.example.demo.oauth2;

import com.example.demo.auth.cookie.RefreshTokenCookieProvider;
import com.example.demo.auth.service.AuthService;
import com.example.demo.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 처리 Handler
 *
 * Spring Security OAuth2 로그인 성공 시 호출되는 클래스이다.
 *
 * 로그인 성공 이후 수행되는 작업
 *
 * 1. 인증된 사용자(CustomOAuth2User) 조회
 * 2. 내부 AccessToken 생성
 * 3. RefreshToken 생성 및 저장
 * 4. RefreshToken을 HttpOnly Cookie로 설정
 * 5. 프론트 SPA로 Redirect
 *
 * 이 프로젝트는 SPA + RefreshToken 기반 인증 구조를 사용한다.
 *
 * 인증 구조
 *
 * AccessToken
 *  - 메모리 저장 (React)
 *  - 짧은 만료 시간
 *
 * RefreshToken
 *  - HttpOnly Cookie 저장
 *  - 서버에서 Rotation 관리
 *
 * 로그인 이후 흐름
 *
 * OAuth2 로그인 성공
 * ↓
 * OAuth2LoginSuccessHandler
 * ↓
 * refreshToken Cookie 발급
 * ↓
 * React SPA redirect
 * ↓
 * React AuthProvider
 * ↓
 * /auth/refresh 호출
 * ↓
 * AccessToken 발급
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * JWT 생성 Provider
     *
     * AccessToken 생성에 사용된다.
     */
    private final JwtProvider jwtProvider;

    /**
     * 인증 서비스
     *
     * RefreshToken 생성 및 저장을 담당한다.
     */
    private final AuthService authService;

    /**
     * Refresh Token provider
     * Refresh Token  생성
     * Refresh Token  삭제
     */
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    /**
     * RefreshToken 쿠키 만료 시간
     */
    @Value("${app.auth.refresh-expire-seconds}")
    private long refreshExpireSeconds;

    /**
     * OAuth2 로그인 성공 시 호출되는 메서드
     *
     * Spring Security 내부 흐름
     *
     * OAuth2LoginAuthenticationFilter
     * ↓
     * Authentication 생성
     * ↓
     * AuthenticationSuccessHandler 호출
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        /**
         * 인증된 사용자 Principal 객체
         *
         * CustomOAuth2UserService에서 생성된 객체이다.
         */
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        /**
         * 내부 시스템 사용자 ID
         */
        Long userId = oAuth2User.getUserId();

        /**
         * accessToken은 여기서 발급하지만 클라이언트에 직접 전달하지 않는다.
         *
         * 이 프로젝트는 SPA + Refresh Token 기반 인증 구조를 사용한다.
         *
         * 로그인 성공 시에는 refreshToken만 HttpOnly Cookie로 발급하고
         * accessToken은 프론트(AuthProvider)가 /auth/refresh 호출을 통해 발급받는다.
         *
         * 이유:
         * 1. accessToken을 URL이나 response body로 노출하지 않기 위함
         * 2. SPA 초기 진입 시 refresh 기반으로 로그인 상태 복원
         * 3. accessToken 탈취 위험 최소화
         */
        String accessToken = jwtProvider.createAccessToken(userId, oAuth2User.getAuthorities());

        /**
         * 브라우저에서 전달된 deviceId 조회
         *
         * React에서 로그인 요청 시
         * X-Device-Id 헤더를 포함할 수 있다.
         *
         * deviceId는 다음 기능에 사용된다.
         *
         * 로그인된 기기 목록 조회
         * 특정 기기 로그아웃
         * 세션 관리
         */
        String deviceId = request.getHeader("X-Device-Id");

        /**
         * deviceId가 없을 경우
         * 서버에서 UUID 생성
         *
         * fallback 처리
         */
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
        }

        /**
         * RefreshToken 생성
         *
         * AuthService 내부에서 수행되는 작업
         *
         * UUID refreshToken 생성
         * RefreshToken hash 생성
         * DB 또는 Redis 저장
         * deviceId / ipAddress / userAgent 저장
         */
        String refreshToken = authService.createRefreshToken(
                userId,
                request,
                deviceId
        );

        /**
         * RefreshToken Cookie 생성
         *
         * HttpOnly Cookie로 설정하여
         * JavaScript 접근을 차단한다.
         *
         * XSS 공격 방지 목적이다.
         */
        /*
        ResponseCookie cookie = refreshTokenCookieProvider.createResponseCookie(
                refreshToken,
                refreshExpireSeconds
        );

        response.addHeader("Set-Cookie", cookie.toString());
        */
        Cookie cookie = refreshTokenCookieProvider.createCookie(
                refreshToken,
                refreshExpireSeconds
        );

        response.addCookie(cookie);

        /**
         * 로그인 성공 후
         * React SPA로 Redirect
         *
         * 이후 React AuthProvider가
         * /auth/refresh 호출을 통해 AccessToken 발급
         */
        response.sendRedirect("http://localhost:5173");
    }
}