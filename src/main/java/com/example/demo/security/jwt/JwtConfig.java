package com.example.demo.security.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 설정 활성화 Config
 *
 * 이 클래스의 역할은
 *
 * JwtProperties 클래스를
 * Spring Boot ConfigurationProperties로 등록하는 것이다.
 *
 * 즉 application.yml 또는 application.properties에 정의된
 *
 * jwt:
 *   secret: xxx
 *   access-token-expiration: xxx
 *
 * 같은 설정값을
 *
 * JwtProperties 클래스에 바인딩하도록 활성화한다.
 *
 * Spring Boot는 @ConfigurationProperties를 사용할 때
 *
 * 1 자동 스캔
 * 2 명시적 활성화
 *
 * 두 가지 방식이 있는데
 *
 * 이 프로젝트에서는
 *
 * EnableConfigurationProperties 방식
 *
 * 을 사용하고 있다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    /**
     * 이 클래스에는 Bean 정의가 없다.
     *
     * 단순히 JwtProperties를 Spring Bean으로 등록하기 위한
     * Configuration 클래스이다.
     *
     * 동작 과정
     *
     * Spring Boot 시작
     * ↓
     * JwtConfig 로딩
     * ↓
     * JwtProperties Bean 등록
     * ↓
     * application.yml 값 바인딩
     */
}