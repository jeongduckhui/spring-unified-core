package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Swagger / OpenAPI 설정
 *
 * 주요 기능
 *
 * 1. API 문서 기본 정보 설정
 * 2. JWT 인증 설정 (Authorization Header)
 * 3. Swagger UI에서 JWT 테스트 가능
 * 4. local, dev 환경에서만 Swagger 활성화
 */
@Configuration
@Profile({"local", "dev"})
public class SwaggerConfig {

    /**
     * Swagger OpenAPI 기본 설정
     */
    @Bean
    public OpenAPI openAPI() {

        // JWT 인증 스키마 이름
        String jwtSchemeName = "JWT";

        return new OpenAPI()

                // API 기본 정보
                .info(apiInfo())

                /*
                -------------------------------------------------
                Server 설정
                -------------------------------------------------

                기본적으로 springdoc-openapi는 현재 접속한 서버 URL을
                자동으로 Generated server url로 생성한다.

                예
                http://localhost:8081/swagger-ui 접속 시
                → http://localhost:8081 서버 자동 설정

                따라서 일반적인 환경에서는 별도 설정이 필요 없다.

                다만 하나의 Swagger UI에서 여러 서버를 테스트하려면
                아래 servers 설정을 사용할 수 있다.

                서버를 선택해도 CORS 문제로 접속이 안될 수도 있다.
                */
                /*
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("LOCAL"),
                        new Server().url("https://api.test.com").description("DEV"),
                        new Server().url("https://api.company.com").description("STAGING")
                ))
                */

                // JWT 인증 요구사항 추가
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))

                // Security Scheme 등록
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        jwtSchemeName,
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    /**
     * API 문서 기본 정보
     */
    private Info apiInfo() {

        return new Info()
                .title("Next Generation API")
                .description("차세대 프로젝트 API 문서")
                .version("1.0.0");
    }
}