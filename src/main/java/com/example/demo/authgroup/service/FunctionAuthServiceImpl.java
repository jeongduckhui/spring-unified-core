package com.example.demo.authgroup.service;

import com.example.demo.authgroup.domain.FunctionAuthGroup;
import com.example.demo.authgroup.repository.FunctionAuthGroupRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FunctionAuthServiceImpl implements FunctionAuthService {

    private final FunctionAuthGroupRepository functionAuthGroupRepository;

    @Qualifier("functionAuthCache")
    private final Cache<String, Object> functionAuthCache;

    @Override
    public Set<Long> getAllowedAuthGroupIds(String functionId) {

        Set<Long> cached = (Set<Long>) functionAuthCache.getIfPresent(functionId);

        if (cached != null) {
            return cached;
        }

        Set<Long> result = functionAuthGroupRepository
                .findByFunctionIdAndUseYn(functionId, "Y")
                .stream()
                .map(FunctionAuthGroup::getAuthGroupId)
                .collect(Collectors.toSet());

        functionAuthCache.put(functionId, result);

        return result;
    }
}