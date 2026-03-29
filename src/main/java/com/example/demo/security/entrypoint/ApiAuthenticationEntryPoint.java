package com.example.demo.security.entrypoint;

import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.example.demo.message.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 인증 실패 처리 EntryPoint
 *
 * 인증되지 않은 사용자가 보호된 API에 접근했을 때
 * JSON 에러 응답을 반환한다.
 */
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String message = messageService.getMessage(
                ExceptionCode.UNAUTHORIZED.getMessageId(),
                ExceptionCode.UNAUTHORIZED.getActionType()
        );

        ErrorResponse error = ErrorResponse.builder()
                .code(ExceptionCode.UNAUTHORIZED.getCode())
                .message(message)
                .build();

        ApiResult<?> body = ApiResult.fail(error);

        objectMapper.writeValue(response.getWriter(), body);
    }
}