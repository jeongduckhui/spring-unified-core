package com.example.demo.security.listener;

import com.example.demo.common.logging.SecurityLogger;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityLoginSuccessListener {

    private final HttpServletRequest request;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {

        String user = event.getAuthentication().getName();

        SecurityLogger.loginSuccess(user, request);
    }
}