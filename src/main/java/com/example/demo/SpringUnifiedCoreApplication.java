package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // 메일 비동기 발송
@EnableRetry // 메일 발송 재시도
@EnableScheduling // Refresh Token 일괄삭제, 매일 재발송 배치
@SpringBootApplication
public class SpringUnifiedCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringUnifiedCoreApplication.class, args);
	}

}
