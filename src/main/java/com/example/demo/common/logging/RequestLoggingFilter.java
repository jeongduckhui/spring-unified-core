package com.example.demo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API 요청 / 응답 로그 Filter
 *
 * 모든 HTTP 요청에 대해
 * 요청 정보와 응답 정보를 로그로 기록하는 역할을 수행한다.
 *
 * Filter 역할
 *
 * 1 클라이언트 IP 기록
 * 2 HTTP Method 기록
 * 3 요청 URI 기록
 * 4 응답 Status 기록
 * 5 API 처리 시간(ms) 기록
 *
 * 출력 예
 *
 * REQUEST ip=127.0.0.1 method=GET uri=/api/users?page=1
 *
 * RESPONSE status=200 duration=12ms uri=/api/users
 *
 * 사용 목적
 *
 * - API 호출 추적
 * - 응답 속도 분석
 * - 장애 발생 시 요청 흐름 확인
 *
 * 로그 파일
 *
 * api.log
 *
 * Filter 순서
 *
 * TraceIdFilter
 * JwtAuthenticationFilter
 * RequestLoggingFilter
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * API 요청 / 응답 로그 처리
     *
     * 모든 HTTP 요청마다 실행된다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /**
         * 요청 URI 조회
         */
        String uri = request.getRequestURI();

        /**
         * 불필요 로그 제외
         *
         * actuator
         * favicon
         */
        if (uri.startsWith("/actuator")
                || uri.equals("/favicon.ico")) {

            filterChain.doFilter(request, response);
            return;
        }

        /**
         * 요청 시작 시간 기록
         */
        long start = System.currentTimeMillis();

        /**
         * 요청 정보 조회
         */
        String method = request.getMethod();
        String query = request.getQueryString();
        String clientIp = getClientIp(request);

        /**
         * QueryString 포함 URI 생성
         */
        String fullUri = query == null ? uri : uri + "?" + query;

        /**
         * 요청 로그 출력
         */
        log.info(
                "REQUEST ip={} method={} uri={}",
                clientIp,
                method,
                fullUri
        );

        try {

            /**
             * 다음 Filter 또는 Controller 실행
             */
            filterChain.doFilter(request, response);

        } finally {

            /**
             * API 처리 시간 계산
             */
            long duration = System.currentTimeMillis() - start;

            /**
             * HTTP 응답 상태 코드
             */
            int status = response.getStatus();

            /**
             * 응답 로그 출력
             */
            log.info(
                    "RESPONSE status={} duration={}ms uri={}",
                    status,
                    duration,
                    uri
            );
        }
    }

    /**
     * 클라이언트 IP 조회
     *
     * 프록시 / 로드밸런서 환경에서는
     * X-Forwarded-For Header를 사용한다.
     *
     * 없는 경우
     * request.getRemoteAddr() 사용
     */
    private String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}