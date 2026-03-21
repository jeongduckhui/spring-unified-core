package com.example.demo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정값 바인딩 클래스
 *
 * 이 클래스는 application.yml 또는 application.properties에 정의된
 * jwt 관련 설정을 Spring Bean으로 바인딩하기 위한 클래스이다.
 *
 * 예시 application.yml
 *
 * jwt:
 *   secret: my-super-secret-key
 *   access-token-expiration-seconds: 3600
 *   issuer: demo-auth-server
 *
 * Spring Boot는 @ConfigurationProperties(prefix = "jwt") 설정을 통해
 * 위 설정값들을 자동으로 이 클래스에 매핑한다.
 *
 * 이 클래스는 record 타입으로 작성되어
 * immutable(불변 객체) 형태로 설정값을 보관한다.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        /**
         * JWT 서명에 사용되는 secret key
         *
         * JWT 생성 시
         *
         * header.payload.signature
         *
         * 구조 중 signature 생성에 사용된다.
         *
         * JWT 검증 시에도 동일한 secret이 필요하다.
         *
         * 실무에서는 다음 조건을 만족해야 한다.
         *
         * - 최소 256bit 이상
         * - 외부 노출 금지
         * - 환경 변수 또는 Secret Manager 사용 권장
         */
        String secret,

        /**
         * AccessToken 만료 시간
         *
         * 단위
         * seconds
         *
         * 예
         *
         * 3600 = 1시간
         *
         * JwtProvider에서
         *
         * now + expirationSeconds
         *
         * 방식으로 토큰 만료 시간이 계산된다.
         */
        long accessTokenExpirationSeconds,

        /**
         * JWT issuer (토큰 발급자)
         *
         * JWT Claim 중
         *
         * iss
         *
         * 필드에 들어가는 값이다.
         *
         * 예
         *
         * iss = demo-auth-server
         *
         * JWT 검증 시
         * issuer 일치 여부를 검사할 수도 있다.
         *
         * 멀티 서비스 환경에서
         * 토큰 발급 서버를 식별할 때 사용된다.
         */
        String issuer

) {
}