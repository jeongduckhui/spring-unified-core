package com.example.demo.message.controller;

import com.example.demo.message.cache.MessageCachePublisher;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/message")
public class MessageAdminController {

    private final MessageCachePublisher publisher;

    @PostMapping("/reload")
    public String reload() {
        publisher.publish();
        return "OK";
    }
}