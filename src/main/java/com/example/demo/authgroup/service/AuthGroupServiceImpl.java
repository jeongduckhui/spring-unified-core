package com.example.demo.authgroup.service;

import com.example.demo.authgroup.domain.UserAuthGroup;
import com.example.demo.authgroup.repository.UserAuthGroupRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthGroupServiceImpl implements AuthGroupService {

    private final UserAuthGroupRepository userAuthGroupRepository;

    @Qualifier("userAuthCache")
    private final Cache<Long, Object> userAuthCache;

    /**
     * 사용자 권한그룹 조회
     *
     * 핵심 로직
     *
     * grantYn = Y 인 것만 진짜 권한
     */
    @Override
    public Set<Long> getUserAuthGroupIds(Long userId) {

        Set<Long> cached = (Set<Long>) userAuthCache.getIfPresent(userId);

        if (cached != null) {
            return cached;
        }

        Set<Long> result = userAuthGroupRepository
                .findByUserIdAndGrantYn(userId, "Y")
                .stream()
                .map(UserAuthGroup::getAuthGroupId)
                .collect(Collectors.toSet());

        userAuthCache.put(userId, result);

        return result;
    }
}