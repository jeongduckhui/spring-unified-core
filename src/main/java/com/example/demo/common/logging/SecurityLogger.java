package com.example.demo.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Security Audit Logger
 *
 * 보안 관련 이벤트를 security.log 파일에 기록하는
 * Security 감사 로그 유틸리티 클래스이다.
 *
 * 기록 이벤트
 *
 * LOGIN_SUCCESS
 * LOGIN_FAIL
 * ACCESS_DENIED
 * LOGOUT
 * TOKEN_REFRESH
 * TOKEN_INVALID
 *
 * 사용 목적
 *
 * - 로그인 이력 추적
 * - 권한 없는 접근 탐지
 * - 토큰 탈취 탐지
 * - 보안 감사 로그 생성
 *
 * 로그 예
 *
 * LOGIN_SUCCESS user=1 ip=127.0.0.1 uri=/login agent=Chrome traceId=ab12cd
 *
 * LOGIN_FAIL user=kim ip=127.0.0.1 uri=/login reason=password_mismatch traceId=ab12cd
 *
 * ACCESS_DENIED user=1 ip=127.0.0.1 uri=/admin traceId=ab12cd
 *
 * TOKEN_REFRESH user=1 device=mobile ip=127.0.0.1 uri=/auth/refresh traceId=ab12cd
 *
 * 로그 파일
 *
 * security.log
 */
public class SecurityLogger {

    /**
     * security.log 전용 Logger
     */
    private static final Logger log = LoggerFactory.getLogger("SECURITY");

    /**
     * 클라이언트 IP 조회
     *
     * 프록시 / 로드밸런서 환경에서는
     * X-Forwarded-For Header 사용
     */
    private static String getIp(HttpServletRequest request) {

        String xf = request.getHeader("X-Forwarded-For");

        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0];
        }

        return request.getRemoteAddr();
    }

    /**
     * User-Agent 조회
     *
     * 클라이언트 브라우저 또는 디바이스 정보
     */
    private static String getAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * MDC에 저장된 traceId 조회
     *
     * 요청 단위 로그 추적을 위해 사용
     */
    private static String traceId() {
        return MDC.get("traceId");
    }

    /**
     * 로그인 성공 로그
     *
     * OAuth2 / OIDC / Form 로그인 성공 시 호출
     */
    public static void loginSuccess(String user, HttpServletRequest req) {

        log.info(
                "LOGIN_SUCCESS user={} ip={} uri={} agent={} traceId={}",
                user,
                getIp(req),
                req.getRequestURI(),
                getAgent(req),
                traceId()
        );
    }

    /**
     * 로그인 실패 로그
     *
     * 인증 실패 시 호출
     *
     * 예
     *
     * - 비밀번호 오류
     * - 계정 없음
     * - 계정 잠금
     */
    public static void loginFail(String user, String reason, HttpServletRequest req) {

        log.warn(
                "LOGIN_FAIL user={} ip={} uri={} reason={} traceId={}",
                user,
                getIp(req),
                req.getRequestURI(),
                reason,
                traceId()
        );
    }

    /**
     * 권한 거부 로그
     *
     * Spring Security에서 AccessDeniedException 발생 시 기록
     */
    public static void accessDenied(String user, HttpServletRequest req) {

        log.warn(
                "ACCESS_DENIED user={} ip={} uri={} traceId={}",
                user,
                getIp(req),
                req.getRequestURI(),
                traceId()
        );
    }

    /**
     * 로그아웃 로그
     *
     * 사용자 로그아웃 시 기록
     */
    public static void logout(String user, HttpServletRequest req) {

        log.info(
                "LOGOUT user={} ip={} uri={} traceId={}",
                user,
                getIp(req),
                req.getRequestURI(),
                traceId()
        );
    }

    /**
     * Refresh Token 재발급 로그
     *
     * AccessToken 재발급 시 호출
     *
     * 목적
     *
     * - 세션 활동 추적
     * - 토큰 탈취 탐지
     */
    public static void tokenRefresh(String user, String deviceId, HttpServletRequest req) {

        log.info(
                "TOKEN_REFRESH user={} device={} ip={} uri={} traceId={}",
                user,
                deviceId,
                getIp(req),
                req.getRequestURI(),
                traceId()
        );
    }

    /**
     * 잘못된 토큰 사용 로그
     *
     * 다음 경우 기록
     *
     * - 서명 오류
     * - 토큰 변조
     * - 토큰 구조 오류
     * - 블랙리스트 토큰
     */
    public static void tokenInvalid(String reason, HttpServletRequest req) {

        log.warn(
                "TOKEN_INVALID ip={} uri={} reason={} traceId={}",
                getIp(req),
                req.getRequestURI(),
                reason,
                traceId()
        );
    }
}