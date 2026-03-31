package com.example.demo.redis.config;

import com.example.demo.commoncode.redis.CommonCodeCacheSubscriber;
import com.example.demo.redis.RedisTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    // 🔥 Subscriber들 (확장 가능)
    private final CommonCodeCacheSubscriber commonCodeSubscriber;
    // private final MessageCacheSubscriber messageSubscriber; (추후)

    // ===== Topic =====

    @Bean("commonCodeTopic")
    public ChannelTopic commonCodeTopic() {
        return new ChannelTopic(RedisTopic.COMMON_CODE_CACHE_EVICT.getTopic());
    }

    // ===== Listener =====

    @Bean("commonCodeListener")
    public MessageListenerAdapter commonCodeListener() {
        return new MessageListenerAdapter(commonCodeSubscriber, "onMessage");
    }

    // ===== Container =====

    @Bean
    public RedisMessageListenerContainer redisContainer(
            @Qualifier("commonCodeListener") MessageListenerAdapter commonCodeListener,
            @Qualifier("commonCodeTopic") ChannelTopic commonCodeTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(commonCodeListener, commonCodeTopic);

        // 확장 자리 (여기 계속 추가하면 됨)
        // container.addMessageListener(messageListener, messageTopic);

        return container;
    }
}