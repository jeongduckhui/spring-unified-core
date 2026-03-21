package com.example.demo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security Audit Filter
 *
 * Spring Security 처리 과정에서 발생하는
 * 보안 이벤트를 기록하는 Filter이다.
 *
 * 현재 감지 이벤트
 *
 * ACCESS_DENIED
 *
 * 동작 흐름
 *
 * 1 요청 처리 진행
 * 2 Controller / Security FilterChain 실행
 * 3 AccessDeniedException 발생 시 감지
 * 4 SecurityLogger를 통해 보안 로그 기록
 * 5 예외를 다시 throw하여 Spring Security가 처리
 *
 * 로그 예
 *
 * ACCESS_DENIED user=1 ip=127.0.0.1 uri=/admin traceId=ab12cd
 *
 * 사용 목적
 *
 * - 권한 없는 접근 탐지
 * - 관리자 페이지 접근 시도 기록
 * - 보안 감사 로그 생성
 *
 * 로그 파일
 *
 * security.log
 *
 * Filter 순서
 *
 * TraceIdFilter
 * JwtAuthenticationFilter
 * RequestLoggingFilter
 * SecurityAuditFilter
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
public class SecurityAuditFilter extends OncePerRequestFilter {

    /**
     * Security Audit 처리
     *
     * 요청 처리 중 AccessDeniedException 발생 시
     * 보안 로그를 기록한다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            /**
             * 다음 Filter 또는 Controller 실행
             */
            filterChain.doFilter(request, response);

        } catch (AccessDeniedException ex) {

            /**
             * 현재 인증 사용자 조회
             */
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            /**
             * 인증 사용자 ID
             *
             * 인증 정보가 없는 경우 anonymous
             */
            String user = auth != null ? auth.getName() : "anonymous";

            /**
             * 권한 거부 로그 기록
             */
            SecurityLogger.accessDenied(user, request);

            /**
             * Spring Security가 처리하도록 예외 다시 전달
             */
            throw ex;
        }
    }
}