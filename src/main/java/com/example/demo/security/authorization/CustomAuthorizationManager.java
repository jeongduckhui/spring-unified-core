package com.example.demo.security.authorization;

import com.example.demo.authgroup.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final AuthorizationService authorizationService;

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context
    ) {

        HttpServletRequest request = context.getRequest();

        Authentication auth = authentication.get();

        /**
         * 인증 안된 경우 → 차단
         */
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        /**
         * JwtAuthenticationFilter에서 넣은 userId
         */
        Object principal = auth.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            return new AuthorizationDecision(false);
        }

        Long userId;

        if (principal instanceof Long) {
            userId = (Long) principal;
        } else if (principal instanceof String) {
            userId = Long.valueOf((String) principal);
        } else {
            throw new IllegalStateException("Invalid principal type");
        }

        String uri = request.getRequestURI();

        /**
         * 🔥 핵심 인가 로직 호출
         */
        boolean allowed = authorizationService.isAuthorized(userId, uri);

        return new AuthorizationDecision(allowed);
    }
}