package com.example.demo.common.exception.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * resources 폴더에 messages_ko.properties 설정을 위한 파일
 * DB에서 메시지 가져오는 기능과는 무관한 파일임
 */
@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {

        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();

        source.setBasename("classpath:exception/message/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);

        return source;
    }
}