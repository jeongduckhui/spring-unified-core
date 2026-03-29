package com.example.demo.message.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageCachePublisher {

    private static final String CHANNEL = "message-cache-reload";

    private final StringRedisTemplate stringRedisTemplate;

    public void publish() {
        stringRedisTemplate.convertAndSend(CHANNEL, "reload");
    }
}