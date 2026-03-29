package com.example.demo.message.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MessageRedisCache {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "message:";

    public String get(String key) {
        return redis.opsForValue().get(PREFIX + key);
    }

    public void set(String key, String value) {
        redis.opsForValue().set(PREFIX + key, value, Duration.ofHours(6));
    }

    public void delete(String key) {
        redis.delete(PREFIX + key);
    }
}