package com.example.demo.security.listener;

import com.example.demo.common.logging.SecurityLogger;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityLoginFailListener {

    private final HttpServletRequest request;

    @EventListener
    public void onFail(AbstractAuthenticationFailureEvent event) {

        String user = event.getAuthentication().getName();
        String reason = event.getException().getMessage();

        SecurityLogger.loginFail(user, reason, request);
    }
}