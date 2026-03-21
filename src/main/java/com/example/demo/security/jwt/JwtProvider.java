package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성 및 검증을 담당하는 핵심 클래스
 *
 * 이 클래스는 다음 기능을 담당한다.
 *
 * 1 AccessToken 생성
 * 2 JWT 유효성 검증
 * 3 JWT Claim 파싱
 * 4 사용자 정보 추출
 * 5 만료 토큰 처리
 *
 * 현재 프로젝트의 모든 JWT 처리는 이 클래스에서 수행된다.
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    /**
     * JWT 설정값
     *
     * application.yml 에서 읽어온다.
     *
     * jwt:
     *   secret:
     *   access-token-expiration-seconds:
     *   issuer:
     */
    private final JwtProperties jwtProperties;

    /**
     * JWT 서명에 사용할 SecretKey
     *
     * 애플리케이션 시작 시 생성된다.
     */
    private SecretKey secretKey;

    /**
     * Bean 생성 후 실행되는 초기화 메서드
     *
     * jwtProperties.secret 값을 기반으로
     * HMAC SHA SecretKey를 생성한다.
     */
    @PostConstruct
    public void init() {

        /**
         * 문자열 secret을 byte 배열로 변환 후
         * HMAC SHA Key로 변환한다.
         */
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * AccessToken 생성
     *
     * payload 구조
     *
     * {
     *   sub: userId
     *   iss: issuer
     *   iat: 발급시간
     *   exp: 만료시간
     *   roles: 권한 목록
     * }
     */
    public String createAccessToken(Long userId, Collection<? extends GrantedAuthority> authorities) {

        /**
         * 현재 시간
         */
        Instant now = Instant.now();

        /**
         * 토큰 만료 시간
         */
        Instant expiry = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        /**
         * GrantedAuthority → String role 변환
         */
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        /**
         * JWT 생성
         */
        return Jwts.builder()

                /**
                 * subject = userId
                 */
                .subject(String.valueOf(userId))

                /**
                 * 토큰 발급자
                 */
                .issuer(jwtProperties.issuer())

                /**
                 * 발급 시간
                 */
                .issuedAt(Date.from(now))

                /**
                 * 만료 시간
                 */
                .expiration(Date.from(expiry))

                /**
                 * 사용자 권한
                 */
                .claim("roles", roles)

                /**
                 * 서명 생성
                 */
                .signWith(secretKey)

                /**
                 * 최종 JWT 문자열 생성
                 */
                .compact();
    }

    /**
     * JWT 유효성 검사
     *
     * 검사 항목
     *
     * - 서명 검증
     * - 토큰 구조
     * - issuer 검증
     * - 만료 검사
     */
    public boolean validateToken(String token) {

        try {

            /**
             * Claims 파싱 시
             * JWT 검증이 동시에 수행된다.
             */
            parseClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {

            /**
             * JWT 오류 발생
             */
            return false;
        }
    }

    /**
     * 토큰 만료 여부 검사
     */
    public boolean isExpired(String token) {

        try {

            parseClaims(token);

            return false;

        } catch (ExpiredJwtException e) {

            return true;

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    /**
     * JWT에서 사용자 ID 추출
     */
    public Long getUserId(String token) {

        Claims claims = parseClaims(token);

        return Long.parseLong(claims.getSubject());
    }

    /**
     * JWT에서 권한 목록 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {

        Claims claims = parseClaims(token);

        Object roles = claims.get("roles");

        if (roles == null) {
            return List.of();
        }

        return (List<String>) roles;
    }

    /**
     * 토큰 남은 만료 시간 계산
     *
     * 로그아웃 시 blacklist TTL 계산에 사용된다.
     */
    public long getRemainingExpirationMillis(String token) {

        Claims claims = parseClaims(token);

        long now = System.currentTimeMillis();

        long expirationTime = claims.getExpiration().getTime();

        return Math.max(expirationTime - now, 0);
    }

    /**
     * JWT Claims 파싱
     *
     * 내부적으로 다음 검증 수행
     *
     * - 서명 검증
     * - issuer 검증
     * - 만료 검사
     */
    public Claims parseClaims(String token) {

        return Jwts.parser()

                /**
                 * 서명 검증 Key
                 */
                .verifyWith(secretKey)

                /**
                 * issuer 검증
                 */
                .requireIssuer(jwtProperties.issuer())

                /**
                 * parser 생성
                 */
                .build()

                /**
                 * JWT 파싱
                 */
                .parseSignedClaims(token)

                /**
                 * payload 반환
                 */
                .getPayload();
    }

    /*
     ---------------------------------------------
     Expired Token 처리
     ---------------------------------------------
     */

    /**
     * 만료 토큰에서도 Claim을 읽기 위한 메서드
     */
    public Claims parseClaimsAllowExpired(String token) {

        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {

            /**
             * 만료 토큰이어도 Claims는 읽을 수 있다.
             */
            return e.getClaims();
        }
    }

    /**
     * 만료 토큰에서 userId 추출
     */
    public Long getUserIdFromExpiredToken(String token) {

        Claims claims = parseClaimsAllowExpired(token);

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 만료 토큰에서 roles 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromExpiredToken(String token) {

        Claims claims = parseClaimsAllowExpired(token);

        Object roles = claims.get("roles");

        if (roles == null) {
            return List.of();
        }

        return (List<String>) roles;
    }
}