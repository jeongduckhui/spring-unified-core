package com.example.demo.security.logout;

import com.example.demo.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security LogoutHandler 구현 클래스
 *
 * 이 클래스는 Spring Security의 LogoutFilter에서 호출되는
 * 로그아웃 처리 로직을 담당한다.
 *
 * 현재 프로젝트에서는 사용하지 않고 있지만,
 * SecurityConfig에서 다음과 같이 연결하면 사용할 수 있다.
 *
 * http.logout()
 *     .addLogoutHandler(customLogoutHandler)
 *
 * 역할
 *
 * 1 refreshToken revoke 처리
 * 2 accessToken blacklist 등록
 * 3 refreshToken 쿠키 제거
 * 4 세션 제거
 * 5 SecurityContext 초기화
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    /**
     * 인증 관련 비즈니스 로직을 담당하는 서비스
     *
     * logout 시 다음 처리를 수행한다.
     *
     * - refreshToken revoke
     * - accessToken blacklist 등록
     */
    private final AuthService authService;

    /**
     * 로그아웃 실행 메서드
     *
     * Spring Security LogoutFilter가 호출한다.
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {

        /**
         * 쿠키에서 refreshToken 추출
         */
        String refreshToken = extractRefreshToken(request);

        /**
         * Authorization header에서 accessToken 추출
         */
        String accessToken = extractAccessToken(request);

        /**
         * 실제 로그아웃 처리
         *
         * AuthService.logout 내부에서
         *
         * refreshToken revoke
         * accessToken blacklist 등록
         *
         * 이 수행된다.
         */
        authService.logout(refreshToken, accessToken);

        /**
         * refreshToken 쿠키 삭제
         */
        Cookie cookie = new Cookie("refreshToken", null);

        /**
         * 쿠키 즉시 삭제
         */
        cookie.setMaxAge(0);

        /**
         * 쿠키 경로 설정
         */
        cookie.setPath("/");

        /**
         * 브라우저에 쿠키 삭제 전달
         */
        response.addCookie(cookie);

        /**
         * 세션 존재 시 제거
         *
         * 현재 프로젝트는
         * SessionCreationPolicy.STATELESS 이지만
         *
         * OAuth2 로그인 과정에서 생성된 세션이 있을 수 있으므로
         * 안전하게 제거한다.
         */
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        /**
         * SecurityContext 제거
         *
         * 현재 요청에서 인증 정보를 제거한다.
         */
        SecurityContextHolder.clearContext();
    }

    /**
     * 쿠키에서 refreshToken 추출
     */
    private String extractRefreshToken(HttpServletRequest request) {

        /**
         * 쿠키가 없는 경우
         */
        if (request.getCookies() == null) {
            return null;
        }

        /**
         * refreshToken 쿠키 탐색
         */
        for (Cookie cookie : request.getCookies()) {

            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * Authorization header에서 accessToken 추출
     */
    private String extractAccessToken(HttpServletRequest request) {

        /**
         * Authorization header 조회
         */
        String header = request.getHeader("Authorization");

        /**
         * Bearer 형식이 아닌 경우
         */
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        /**
         * Bearer 제거 후 토큰 반환
         */
        return header.substring(7);
    }
}