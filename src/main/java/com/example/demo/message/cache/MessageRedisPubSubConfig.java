package com.example.demo.message.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

// redis 패키지 안에 있는 통합 RedisPubSubConfig를 사용해야 함.
// 예시는 CommonCode에 있음. 공통코드는 개별 publisher, subscriber 사용하지 않고 있음
@Configuration
public class MessageRedisPubSubConfig {

    @Bean("messageCacheReloadTopic")
    public ChannelTopic messageCacheReloadTopic() {
        return new ChannelTopic("message-cache-reload");
    }

    @Bean("messageCacheReloadListenerAdapter")
    public MessageListenerAdapter listenerAdapter(
            MessageCacheSubscriber subscriber
    ) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer container(
            RedisConnectionFactory connectionFactory,
            @Qualifier("messageCacheReloadListenerAdapter") MessageListenerAdapter listenerAdapter,
            @Qualifier("messageCacheReloadTopic") ChannelTopic topic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic);
        return container;
    }
}