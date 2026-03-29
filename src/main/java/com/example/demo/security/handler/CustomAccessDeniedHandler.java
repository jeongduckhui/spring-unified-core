package com.example.demo.security.handler;

import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.example.demo.message.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        String message = messageService.getMessage(
                ExceptionCode.FORBIDDEN.getMessageId(),
                ExceptionCode.FORBIDDEN.getActionType()
        );

        ErrorResponse error = ErrorResponse.builder()
                .code(ExceptionCode.FORBIDDEN.getCode())
                .message(message)
                .build();

        ApiResult<?> body = ApiResult.fail(error);

        objectMapper.writeValue(response.getWriter(), body);
    }
}