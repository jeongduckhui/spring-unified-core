package com.example.demo.security.entrypoint;

import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security 인증 실패 처리 EntryPoint
 *
 * 인증되지 않은 사용자가 보호된 API에 접근했을 때
 * 실행되는 처리 클래스이다.
 *
 * Spring Security 기본 동작
 *
 * 인증 실패
 * ↓
 * 로그인 페이지 Redirect
 *
 * 하지만 현재 프로젝트는
 *
 * SPA + REST API 구조
 *
 * 이기 때문에 Redirect 대신
 * JSON 응답을 반환하도록 구현하였다.
 *
 * 동작 흐름
 *
 * 인증되지 않은 요청 발생
 * ↓
 * Spring Security ExceptionTranslationFilter
 * ↓
 * AuthenticationEntryPoint 호출
 * ↓
 * JSON 에러 응답 반환
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * JSON 직렬화 도구
     *
     * Map 객체를 JSON 문자열로 변환할 때 사용된다.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 실패 시 호출되는 메서드
     *
     * Spring Security 내부 흐름
     *
     * 요청
     * ↓
     * JwtAuthenticationFilter
     * ↓
     * 인증 실패
     * ↓
     * ExceptionTranslationFilter
     * ↓
     * AuthenticationEntryPoint 호출
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        /**
         * HTTP 상태 코드 설정
         *
         * 401 Unauthorized
         *
         * 의미
         *
         * 인증이 필요하지만 인증되지 않음
         */
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        /**
         * 응답 Content-Type 설정
         *
         * JSON 형식으로 응답
         */
        response.setContentType("application/json;charset=UTF-8");

        /**
         * JSON 문자열로 변환하여 응답 작성
         */
        ErrorResponse error = ErrorResponse.builder()
                .code(ExceptionCode.UNAUTHORIZED.getCode())
                .message(ExceptionCode.UNAUTHORIZED.getMessage())
                .build();

        ApiResult<?> body = ApiResult.fail(error);

        objectMapper.writeValue(response.getWriter(), body);
    }
}