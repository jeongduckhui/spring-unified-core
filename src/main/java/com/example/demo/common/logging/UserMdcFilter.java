package com.example.demo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * User MDC 설정 Filter
 *
 * Spring Security 인증 정보를 기반으로
 * 현재 로그인한 사용자 ID를 MDC에 저장하는 Filter이다.
 *
 * MDC(Mapped Diagnostic Context)는
 * 로그 출력 시 공통적으로 포함되는 정보를 저장하는 기능이다.
 *
 * 본 Filter는 로그인된 사용자 ID를 MDC에 등록하여
 * 모든 로그에 userId가 자동으로 포함되도록 한다.
 *
 * 동작 흐름
 *
 * 1 SecurityContext에서 Authentication 조회
 * 2 인증 사용자 principal 조회
 * 3 MDC에 userId 저장
 * 4 요청 처리 진행
 * 5 요청 종료 후 MDC에서 userId 제거
 *
 * 로그 예
 *
 * [traceId=ab12cd userId=1] REQUEST ip=127.0.0.1 method=GET uri=/api/users
 *
 * 사용 목적
 *
 * - 사용자 단위 로그 추적
 * - 장애 분석 시 사용자 활동 추적
 * - 보안 감사 로그 강화
 *
 * 적용 로그
 *
 * api.log
 * sql.log
 * security.log
 * error.log
 *
 * Filter 순서
 *
 * TraceIdFilter
 * JwtAuthenticationFilter
 * UserMdcFilter
 * RequestLoggingFilter
 * SecurityAuditFilter
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class UserMdcFilter extends OncePerRequestFilter {

    /**
     * MDC userId 설정 처리
     *
     * 모든 HTTP 요청마다 실행된다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            /**
             * 현재 인증 사용자 조회
             */
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            /**
             * 인증 정보가 존재하는 경우
             * 사용자 ID를 MDC에 저장
             */
            if (authentication != null && authentication.getPrincipal() != null) {

                Object principal = authentication.getPrincipal();

                MDC.put("userId", principal.toString());
            }

            /**
             * 다음 Filter 또는 Controller 실행
             */
            filterChain.doFilter(request, response);

        } finally {

            /**
             * 요청 종료 후 MDC userId 제거
             *
             * Thread 재사용 환경에서
             * 사용자 정보가 남지 않도록 방지
             */
            MDC.remove("userId");
        }
    }
}