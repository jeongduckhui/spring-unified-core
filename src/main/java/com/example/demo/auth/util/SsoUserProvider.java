package com.example.demo.auth.util;

import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SsoUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    }
}