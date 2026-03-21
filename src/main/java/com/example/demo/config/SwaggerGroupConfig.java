package com.example.demo.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Swagger API 그룹 설정
 *
 * Swagger UI에서 API를 기능별로 분리하여 표시
 */
@Configuration
@Profile({"local", "dev"})
public class SwaggerGroupConfig {

    /**
     * 인증 관련 API
     */
    @Bean
    public GroupedOpenApi authApi() {

        return GroupedOpenApi.builder()
                .group("Auth API")
                .pathsToMatch("/auth/**")
//                .pathsToExclude("/internal/**") // 숨길려는 url 추가. 혹은 클래스, 메서드 단위로 @Hidden 어노테이션 사용하면 숨겨짐 @Hidden 을 가장 많이 사용함
//                .packagesToScan("com.example.demo.api") // 패키지 단위로 스캔할 수도 있음.
//                .packagesToExclude("com.example.demo.internal") // 패키지 단위로 숨길수도 있음.
                .build();
    }

    /**
     * 사용자 API
     */
    @Bean
    public GroupedOpenApi userApi() {

        return GroupedOpenApi.builder()
                .group("User API")
                .pathsToMatch(
                        "/api/users/**",
                        "/users/**"
                )
                .build();
    }

    /**
     * 관리자 API
     */
    @Bean
    public GroupedOpenApi adminApi() {

        return GroupedOpenApi.builder()
                .group("Admin API")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /**
     * 시스템 API
     */
    @Bean
    public GroupedOpenApi systemApi() {

        return GroupedOpenApi.builder()
                .group("System API")
                .pathsToMatch("/system/**")
                .build();
    }
}