package com.example.demo.authgroup.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpAuthorizationService implements AuthorizationService {

    @Override
    public boolean isAuthorized(Long userId, String requestUri) {

        /**
         * 항상 허용
         */
        return true;
    }
}