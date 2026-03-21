package com.example.demo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 생성 Filter
 *
 * 모든 HTTP 요청에 대해 고유한 traceId를 생성하여
 * MDC(Mapped Diagnostic Context)에 저장하는 Filter이다.
 *
 * traceId는 하나의 요청(Request)에서 발생하는
 * 모든 로그를 하나로 묶어 추적할 수 있도록 해준다.
 *
 * 동작 흐름
 *
 * 1 요청 시작 시 traceId 생성
 * 2 MDC에 traceId 저장
 * 3 Response Header(X-Trace-Id)에 traceId 추가
 * 4 요청 처리 진행
 * 5 요청 종료 후 MDC에서 traceId 제거
 *
 * 로그 예
 *
 * [traceId=73c8d4bf userId=1] REQUEST ip=127.0.0.1 method=GET uri=/api/users
 *
 * 사용 목적
 *
 * - 요청 단위 로그 추적
 * - 장애 분석 시 요청 흐름 확인
 * - 분산 환경에서 요청 추적
 *
 * 응답 Header
 *
 * X-Trace-Id: 73c8d4bf
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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * MDC Key
     */
    private static final String TRACE_ID = "traceId";

    /**
     * TraceId 생성 및 MDC 등록
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /**
         * 요청 추적을 위한 TraceId 생성
         *
         * UUID 기반으로 생성하고
         * 로그 가독성을 위해 앞 8자리만 사용
         */
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        /**
         * MDC에 traceId 저장
         *
         * 이후 모든 로그에 자동 포함
         */
        MDC.put(TRACE_ID, traceId);

        /**
         * 응답 Header에 TraceId 추가
         *
         * 클라이언트에서도 요청 추적 가능
         */
        response.setHeader("X-Trace-Id", traceId);

        try {

            /**
             * 다음 Filter 또는 Controller 실행
             */
            filterChain.doFilter(request, response);

        } finally {

            /**
             * 요청 종료 후 MDC traceId 제거
             *
             * Thread 재사용 환경에서
             * 이전 요청 traceId가 남지 않도록 방지
             */
            MDC.remove(TRACE_ID);
        }
    }
}