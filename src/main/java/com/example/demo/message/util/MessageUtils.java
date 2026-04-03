package com.example.demo.message.util;

import com.example.demo.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {

    private static MessageService messageService;

    public MessageUtils(MessageService service) {
        MessageUtils.messageService = service;
    }

    public static String get(String id) {
        return messageService.getMessage(id);
    }

    public static String get(String id, String action) {
        return messageService.getMessage(id, action);
    }

    public static String get(String id, String action, Object... args) {
        return messageService.getMessage(id, action, "ko", args);
    }

}
