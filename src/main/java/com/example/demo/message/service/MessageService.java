package com.example.demo.message.service;

import com.example.demo.message.cache.MessageRedisCache;
import com.example.demo.message.constants.MessageActionType;
import com.example.demo.message.domain.MessageEntity;
import com.example.demo.message.repository.MessageRepository;
import com.example.demo.message.util.MessageKeyUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Qualifier("messageLocalCache")
    private final Cache<String, String> localCache;

    private final MessageRedisCache redisCache;

    private final MessageRepository repository;

    private static final String DEFAULT_LANG = "ko";

    public String getMessage(String messageId) {
        return getMessage(messageId, MessageActionType.COMMON, DEFAULT_LANG);
    }

    public String getMessage(String messageId, String actionType) {
        return getMessage(messageId, actionType, DEFAULT_LANG);
    }

    public String getMessage(String messageId, String actionType, String lang, Object... args) {

        String message = getMessageInternal(messageId, actionType, lang);

        if (args != null && args.length > 0) {
            return MessageFormat.format(message, args);
        }

        return message;
    }

    private String getMessageInternal(String messageId, String action, String lang) {

        // 1. exact match
        String msg = getFromCache(messageId, lang, action);
        if (msg != null) return msg;

        // 2. fallback → COMMON
        if (!MessageActionType.COMMON.equals(action)) {
            msg = getFromCache(messageId, lang, MessageActionType.COMMON);
            if (msg != null) return msg;
        }

        // 3. fallback → default language
        if (!DEFAULT_LANG.equals(lang)) {
            msg = getFromCache(messageId, DEFAULT_LANG, MessageActionType.COMMON);
            if (msg != null) return msg;
        }

        // 4. 최종 fallback
        return messageId;
    }

    private String getFromCache(String messageId, String lang, String action) {

        String key = MessageKeyUtil.build(messageId, lang, action);

        // 1. Caffeine
        String msg = localCache.getIfPresent(key);
        if (msg != null) return msg;

        // 2.  Redis
        msg = redisCache.get(key);
        if (msg != null) {
            localCache.put(key, msg);
            return msg;
        }

        // 3. DB
        msg = repository.findById(
                new com.example.demo.message.domain.MessageId(messageId, lang, action)
        ).map(MessageEntity::getMessageText).orElse(null);

        if (msg != null) {
            redisCache.set(key, msg);
            localCache.put(key, msg);
        }

        return msg;
    }
}