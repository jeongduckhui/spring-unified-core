package com.example.demo.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * AccessToken 블랙리스트 Redis 구현체
 *
 * JWT 인증 방식은 기본적으로 Stateless 구조이기 때문에
 * AccessToken을 서버에서 저장하지 않는다.
 *
 * 문제
 *
 * 사용자가 로그아웃하더라도 이미 발급된 AccessToken은
 * 만료될 때까지 계속 사용할 수 있다.
 *
 * 예
 *
 * AccessToken 만료시간 30분
 *
 * 사용자가 로그아웃
 *
 * 이미 발급된 AccessToken은 30분 동안 계속 사용 가능
 *
 * 해결 방법
 *
 * AccessToken Blacklist
 *
 * 로그아웃 시 AccessToken을 Redis에 저장하고
 * 이후 요청에서 해당 토큰이 Redis에 존재하면
 * 인증을 거부한다.
 *
 * Redis를 사용하는 이유
 *
 * TTL 기능이 있기 때문에
 * AccessToken 남은 만료 시간 동안만 저장할 수 있다.
 *
 * 만료 후 자동 삭제되므로
 * 별도의 정리 작업이 필요 없다.
 */
@Service
@RequiredArgsConstructor
public class RedisAccessTokenBlacklistService implements AccessTokenBlacklistService {

    /**
     * Redis Key Prefix
     *
     * Redis Key 충돌을 방지하기 위해 Prefix를 사용한다.
     *
     * 예
     *
     * blacklist:access:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     */
    private static final String PREFIX = "blacklist:access:";

    /**
     * Redis 접근을 위한 RedisTemplate
     *
     * Spring Data Redis에서 제공하는
     * Redis 작업 추상화 클래스
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * AccessToken을 블랙리스트에 등록
     *
     * 로그아웃 시 호출된다.
     *
     * 저장 구조 예
     *
     * key
     * blacklist:access:{accessToken}
     *
     * value
     * "blacklisted"
     *
     * TTL
     * AccessToken 남은 만료 시간
     */
    @Override
    public void blacklist(String accessToken, long ttlMillis) {

        /**
         * accessToken이 null 또는 빈 문자열이면
         * 처리하지 않는다.
         */
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        /**
         * TTL이 0 이하인 경우 저장하지 않는다.
         *
         * 이유
         *
         * 이미 만료된 토큰일 가능성이 있기 때문이다.
         */
        if (ttlMillis <= 0) {
            return;
        }

        /**
         * Redis에 블랙리스트 토큰 저장
         *
         * key
         * blacklist:access:{token}
         *
         * value
         * "blacklisted"
         *
         * TTL
         * accessToken 남은 만료 시간
         */
        redisTemplate.opsForValue().set(
                PREFIX + accessToken,
                "blacklisted",
                Duration.ofMillis(ttlMillis)
        );
    }

    /**
     * AccessToken이 블랙리스트에 존재하는지 검사
     *
     * JwtAuthenticationFilter에서 호출될 가능성이 높다.
     *
     * 인증 흐름 예
     *
     * Authorization Header에서 AccessToken 추출
     *
     * JWT 검증
     *
     * 블랙리스트 검사
     *
     * 블랙리스트 존재 시 인증 실패
     */
    @Override
    public boolean isBlacklisted(String accessToken) {

        /**
         * accessToken이 null 또는 빈 문자열이면
         * 블랙리스트로 간주하지 않는다.
         */
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        /**
         * Redis에서 해당 key 존재 여부 확인
         *
         * exists == true
         * 블랙리스트 토큰
         */
        Boolean exists = redisTemplate.hasKey(PREFIX + accessToken);

        /**
         * Boolean.TRUE.equals를 사용하는 이유
         *
         * RedisTemplate은 null을 반환할 수도 있기 때문에
         * 안전한 Boolean 비교를 위해 사용한다.
         */
        return Boolean.TRUE.equals(exists);
    }
}