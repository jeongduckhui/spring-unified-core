package com.example.demo.commoncode.service;

import com.example.demo.commoncode.dto.CommonCodeCacheEvictMessage;
import com.example.demo.commoncode.redis.CommonCodeCacheEventType;
import com.example.demo.redis.RedisPublisher;
import com.example.demo.redis.RedisTopic;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCodeCacheService {

    private final Cache<String, Object> commonCodeCache;
    private final RedisPublisher publisher;

    private String key(String groupCode) {
        return "commonCode:" + groupCode;
    }

    public void evict(String groupCode) {
        evictLocalOnly(groupCode);
        publisher.publish(
                RedisTopic.COMMON_CODE_CACHE_EVICT,
                new CommonCodeCacheEvictMessage(CommonCodeCacheEventType.EVICT, groupCode)
        );
    }

    public void evictAll() {
        evictAllLocalOnly();
        publisher.publish(
                RedisTopic.COMMON_CODE_CACHE_EVICT,
                new CommonCodeCacheEvictMessage(
                        CommonCodeCacheEventType.EVICT_ALL,
                        null
                )
        );
    }

    public void evictLocalOnly(String groupCode) {
        commonCodeCache.invalidate(key(groupCode));
        log.info("LOCAL CACHE EVICT - {}", groupCode);
    }

    public void evictAllLocalOnly() {
        commonCodeCache.invalidateAll();
        log.info("LOCAL CACHE EVICT ALL");
    }
}