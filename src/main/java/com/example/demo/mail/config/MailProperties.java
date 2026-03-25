package com.example.demo.mail.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MailProperties {

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.enabled}")
    private boolean enabled;

    @Value("${app.mail.max-retry-count:3}")
    private int maxRetryCount;
}