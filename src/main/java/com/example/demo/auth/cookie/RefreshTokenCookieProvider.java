package com.example.demo.auth.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

    private static final String COOKIE_NAME = "refreshToken";

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.domain:}")
    private String domain;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    public Cookie createCookie(String refreshToken, long maxAgeSeconds) {
        /**
         * RefreshToken Cookie 생성
         * HttpOnly Cookie 설정
         * JavaScript 접근 차단
         */
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        /**
         * HttpOnly 설정
         * JavaScript에서 Cookie 접근 불가
         */
        refreshCookie.setHttpOnly(true);

        /**
         * HTTPS 전용 Cookie 여부
         * 현재 개발환경이므로 false
         * 운영 환경에서는 true 권장
         */
        refreshCookie.setSecure(secure);

        /**
         * Cookie 적용 경로
         * "/" 설정 시
         * 모든 API 요청에 Cookie 포함
         */
        refreshCookie.setPath("/");

        /**
         * Cookie 만료 시간 설정
         * 14일
         */
        refreshCookie.setMaxAge((int) maxAgeSeconds);

        return refreshCookie;

    }

    public Cookie deleteCookie() {

        /**
         * RefreshToken 쿠키 삭제
         */
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        return cookie;
    }

    public ResponseCookie createResponseCookie(String refreshToken, long maxAgeSeconds) {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        return builder.build();
    }

    public ResponseCookie deleteResponseCookie() {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite);

        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }

        return builder.build();
    }
}