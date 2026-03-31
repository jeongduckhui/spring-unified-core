package com.example.demo.commoncode.service;

import com.example.demo.commoncode.dto.CommonCodeCacheStatsDto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonCodeCacheMonitorService {

    private final Cache<String, Object> commonCodeCache;

    public CommonCodeCacheStatsDto getStats() {
        CacheStats stats = commonCodeCache.stats();

        return new CommonCodeCacheStatsDto(
                stats.hitCount(),
                stats.missCount(),
                stats.loadSuccessCount(),
                stats.evictionCount(),
                stats.hitRate(),
                commonCodeCache.estimatedSize()
        );
    }
}