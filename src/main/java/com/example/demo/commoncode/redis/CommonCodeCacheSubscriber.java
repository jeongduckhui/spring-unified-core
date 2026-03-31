package com.example.demo.commoncode.redis;

import com.example.demo.commoncode.dto.CommonCodeCacheEvictMessage;
import com.example.demo.commoncode.service.CommonCodeCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonCodeCacheSubscriber {

    private final ObjectMapper objectMapper;
    private final CommonCodeCacheService cacheService;

    public void onMessage(String message) {
        try {
            CommonCodeCacheEvictMessage event =
                    objectMapper.readValue(message, CommonCodeCacheEvictMessage.class);

            switch (event.getType()) {

                case EVICT_ALL -> {
                    cacheService.evictAllLocalOnly();
                    log.info("REDIS SUB - CACHE EVICT ALL");
                }

                case EVICT -> {
                    cacheService.evictLocalOnly(event.getGroupCode());
                    log.info("REDIS SUB - CACHE EVICT {}", event.getGroupCode());
                }
            }

        } catch (Exception e) {
            log.error("REDIS SUB 처리 실패", e);
        }
    }
}