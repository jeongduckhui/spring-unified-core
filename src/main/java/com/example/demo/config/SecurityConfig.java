package com.example.demo.config;

import com.example.demo.oauth2.CustomOAuth2UserService;
import com.example.demo.oauth2.CustomOidcUserService;
import com.example.demo.oauth2.OAuth2LoginSuccessHandler;
import com.example.demo.oauth2.OidcLoginSuccessHandler;
import com.example.demo.security.authorization.CustomAuthorizationManager;
import com.example.demo.security.entrypoint.ApiAuthenticationEntryPoint;
import com.example.demo.security.handler.CustomAccessDeniedHandler;
import com.example.demo.security.jwt.JwtAuthenticationFilter;
import com.example.demo.security.logout.CustomLogoutHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 메인 설정 클래스
 *
 * 이 프로젝트의 인증 구조
 *
 * OAuth2 / OIDC Login
 *        ↓
 * 로그인 성공 Handler
 *        ↓
 * 내부 JWT 발급 (AccessToken + RefreshToken)
 *        ↓
 * 이후 요청은 JWT 기반 인증
 *
 * 특징
 * - 세션 사용하지 않음 (STATELESS)
 * - AccessToken : Authorization Header
 * - RefreshToken : HttpOnly Cookie
 *
 * 주요 기능
 * 1. OAuth2 / OIDC 로그인 처리
 * 2. JWT 인증 필터 등록
 * 3. CORS 설정
 * 4. API 인증 실패 처리
 */
@Configuration
@EnableMethodSecurity // @PreAuthorize 등 Method Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 인증 필터
     *
     * 역할
     * - Authorization: Bearer 토큰 검증
     * - JWT → Authentication 생성
     * - SecurityContext 등록
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * OIDC 로그인 시 사용자 정보 처리 서비스
     */
    private final CustomOidcUserService customOidcUserService;

    /**
     * OIDC 로그인 성공 Handler
     *
     * 역할
     * - AccessToken 생성
     * - RefreshToken 생성 및 저장
     * - RefreshToken Cookie 설정
     */
    private final OidcLoginSuccessHandler oidcLoginSuccessHandler;

    /*
    // OAuth2 로그인용 (현재 미사용)

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomLogoutHandler customLogoutHandler;
    */

    /**
     * 인증 실패 처리 Handler
     *
     * JWT가 없거나
     * JWT가 유효하지 않을 때
     * API 응답을 JSON 형태로 반환
     */
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    /**
     * 인가 실패 처리 Handler
     *
     * 인증은 되었지만
     * 권한이 없는 API에 접근했을 때 실행된다.
     *
     * 예
     * - USER 권한 사용자가 ADMIN API 접근
     * - @PreAuthorize 권한 조건 불일치
     *
     * 주요 역할
     * - 403 Forbidden 응답 반환
     * - SecurityLogger를 통한 보안 로그 기록
     */
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * 권한그룹 체크
     * properties 파일 app.auth.enabled 설정으로 활성화/비활성화 가능
     * 
     */
    private final CustomAuthorizationManager customAuthorizationManager;

    /**
     * Swagger 활성화 여부
     * 로컬: 활성화
     * 운영: 비활성화
     */
    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;

    /**
     * Spring Security Filter Chain 설정
     *
     * Spring Security의 핵심 설정
     *
     * 주요 구성
     * - CSRF 비활성화
     * - FormLogin 비활성화
     * - HTTP Basic 비활성화
     * - OAuth2 Login 설정
     * - JWT 필터 추가
     * - Stateless Session 정책
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // ============================
        // 기본 보안 기능 비활성화
        // ============================

        http
                .csrf(csrf -> csrf.disable()) // JWT 기반 API이므로 CSRF 사용하지 않음
                .formLogin(form -> form.disable()) // form login 사용 안함
                .httpBasic(basic -> basic.disable()); // basic auth 사용 안함


        // ============================
        // CORS 설정
        // ============================

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));


        // ============================
        // URL 권한 설정
        // ============================
        http.authorizeHttpRequests(auth -> {

            // 인증 없이 접근 가능한 URL
            auth.requestMatchers(
                    "/",
                    "/error",
                    "/login/**",
                    "/oauth2/**",
                    "/auth/refresh",
                    "/ws/**",
                    "/test/**",
                    "/user-access-log/end"
            ).permitAll();

            // Swagger 접근 제어
            if (swaggerEnabled) {
                auth.requestMatchers(
                        "/swagger-ui/**", // Swagger
                        "/v3/api-docs/**"          // Swagger
                ).permitAll();
            } else {
                auth.requestMatchers(
                        "/swagger-ui/**", // Swagger
                        "/v3/api-docs/**"          // Swagger
                ).denyAll();
            }

            // 나머지 API는 인증 필요
            
            // 권한그룹 체크가 필요없는 경우
            auth.anyRequest().authenticated();
            // 권한그룹 체크가 필요한 경우, 권한그룹체크 매니저를 추가
//            auth.anyRequest().access(customAuthorizationManager);
        });



        // ============================
        // OAuth2 / OIDC 로그인 설정
        // ============================

        http
                .oauth2Login(oauth -> oauth

                        // OIDC 사용자 정보 처리
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )

                        // 로그인 성공 시 실행 Handler
                        .successHandler(oidcLoginSuccessHandler)
                );


        // ============================
        // 세션 정책
        // ============================

        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        /*
        // ============================
        // Spring Security Logout
        // ============================

        현재 프로젝트에서는 logout을
        Controller에서 처리하기 때문에 비활성화

        이유
        - RefreshToken 삭제
        - Redis / DB Token revoke
        - Device Session 관리

        http
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                );
        */


        // ============================
        // JWT 인증 필터 등록
        // ============================

        /**
         * JwtAuthenticationFilter 실행 위치
         *
         * UsernamePasswordAuthenticationFilter 이전에 실행
         *
         * 이유
         * - 요청에 JWT가 있다면 먼저 인증 처리
         */
        http
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);


        // ============================
        // 인증 / 인가 실패 처리
        // ============================

        /**
         * authenticationEntryPoint
         * - 인증 실패 (401)
         * - JWT 없음 / JWT 만료 / JWT 오류
         *
         * accessDeniedHandler
         * - 인가 실패 (403)
         * - 인증은 되었지만 권한 부족
         */
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }


    /**
     * CORS 설정
     *
     * React SPA → Spring Boot API 호출 허용
     *
     * 현재 구조
     *
     * React
     * http://localhost:5173
     *
     * Backend
     * http://localhost:8081
     *
     * AccessToken → Authorization Header
     * RefreshToken → Cookie
     *
     * Cookie 전달을 위해 allowCredentials 필수
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // 허용 Origin
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        // 허용 HTTP Method
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // 허용 Header
        configuration.setAllowedHeaders(List.of("*"));

        /**
         * Credentials 허용
         *
         * 이유
         * RefreshToken이 Cookie로 전달되기 때문
         */
        configuration.setAllowCredentials(true);

        /**
         * 브라우저에서 접근 가능한 Response Header
         */
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Set-Cookie"
        ));

        // Preflight 캐싱 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 API 경로에 CORS 적용
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}