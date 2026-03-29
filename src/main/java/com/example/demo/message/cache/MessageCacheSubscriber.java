package com.example.demo.message.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCacheSubscriber {

    private final Cache<String, String> messageLocalCache;

    public void onMessage(String message) {
        log.info("Received message cache reload event: {}", message);
        messageLocalCache.invalidateAll();
    }
}