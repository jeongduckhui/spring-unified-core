package com.example.demo.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(RedisTopic topic, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);

            redisTemplate.convertAndSend(topic.getTopic(), payload);

            log.info("REDIS PUB - topic={}, payload={}", topic, payload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Redis publish 실패", e);
        }
    }
}