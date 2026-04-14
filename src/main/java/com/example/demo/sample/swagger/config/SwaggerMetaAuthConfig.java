package com.example.demo.sample.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

//@Configuration
public class SwaggerMetaAuthConfig {

    private static final String META_HEADER = "META";

    @Bean
    public OpenAPI openAPI() {

        SecurityScheme metaScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)   // 핵심
                .in(SecurityScheme.In.HEADER)       // 헤더 방식
                .name(META_HEADER)                  // 헤더 이름
                .name("META")
                .description("META token (개발용 기본값 자동 세팅됨)")
                .extensions(Map.of(
                        "x-default", "dev-meta-token-123"   // 참고용 (UI 반영은 안됨)
                ));

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("META_AUTH", metaScheme))
                .addSecurityItem(new SecurityRequirement().addList("META_AUTH"));
    }
}
