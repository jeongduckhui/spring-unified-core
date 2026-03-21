package com.example.demo.authgroup.service;

import com.example.demo.authgroup.mapper.FunctionUrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true")
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AuthGroupService authGroupService;
    private final FunctionAuthService functionAuthService;
    private final FunctionUrlMapper functionUrlMapper;

    @Override
    public boolean isAuthorized(Long userId, String requestUri) {

        String functionId = functionUrlMapper.getFunctionId(requestUri);

        if (functionId == null) {
            return true; // 정책 선택 가능
        }

        return authGroupService.getUserAuthGroupIds(userId)
                .stream()
                .anyMatch(
                        functionAuthService.getAllowedAuthGroupIds(functionId)::contains
                );
    }
}