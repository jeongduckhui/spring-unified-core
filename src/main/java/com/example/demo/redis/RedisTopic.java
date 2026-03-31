package com.example.demo.redis;

public enum RedisTopic {

    COMMON_CODE_CACHE_EVICT("common-code-cache-evict"),
    MESSAGE_CACHE_RELOAD("message-cache-reload");

    private final String topic;

    RedisTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}