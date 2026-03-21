package com.example.demo.security.handler;

import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.common.logging.SecurityLogger;
import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * JSON 직렬화 도구
     *
     * Map 객체를 JSON 문자열로 변환할 때 사용된다.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse error = ErrorResponse.builder()
                .code(ExceptionCode.FORBIDDEN.getCode())
                .message(ExceptionCode.FORBIDDEN.getMessage())
                .build();

        ApiResult<?> body = ApiResult.fail(error);

        objectMapper.writeValue(response.getWriter(), body);
    }
}