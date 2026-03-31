package com.example.demo.commoncode.service;

import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.provider.CommonCodeProvider;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private final List<CommonCodeProvider> providers;
    private final Cache<String, Object> commonCodeCache;

    private Map<String, CommonCodeProvider> providerMap;

    @PostConstruct
    public void init() {
        providerMap = new HashMap<>();
        for (CommonCodeProvider p : providers) {
            providerMap.put(p.getGroupCode(), p);
        }
    }

    public List<CommonCodeDto> getCode(String groupCode) {

        String key = "commonCode:" + groupCode;

        List<CommonCodeDto> cached =
                (List<CommonCodeDto>) commonCodeCache.getIfPresent(key);

        if (cached != null) {
            log.info("CACHE HIT - {}", groupCode);
            return cached;
        }

        log.info("CACHE MISS - DB 조회 - {}", groupCode);

        CommonCodeProvider provider = providerMap.get(groupCode);

        if (provider == null) {
            throw new IllegalArgumentException("Unknown groupCode: " + groupCode);
        }

        List<CommonCodeDto> result = provider.getCodes();

        commonCodeCache.put(key, result);

        return result;
    }

    public Map<String, List<CommonCodeDto>> getCodes(List<String> groupCodes) {

        Map<String, List<CommonCodeDto>> result = new HashMap<>();

        for (String groupCode : groupCodes) {
            result.put(groupCode, getCode(groupCode));
        }

        return result;
    }
}