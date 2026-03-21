package com.example.demo.auth.service;

import com.example.demo.auth.dto.DeviceSessionResponse;
import com.example.demo.auth.exception.*;
import com.example.demo.token.domain.RefreshToken;
import com.example.demo.token.redis.RedisRefreshToken;
import com.example.demo.token.util.RefreshTokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "app.refresh-token.store", havingValue = "redis")
@RequiredArgsConstructor
public class RefreshTokenRedisService implements RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenHashUtil hashUtil;

    private static final String TOKEN_PREFIX = "refresh:token:";
    private static final String USER_PREFIX = "refresh:user:";
    private static final String REVOKED_PREFIX = "refresh:revoked:";
    private static final String LOCK_PREFIX = "refresh:lock:";

    @Value("${app.auth.refresh-expire-seconds}")
    private long refreshExpireSeconds;

    // =========================
    // 생성
    // =========================
    @Override
    public void createAndSave(
            Long userId,
            String rawRefreshToken,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent,
            String deviceId,
            Long parentTokenId
    ) {

        String hash = hashUtil.hash(rawRefreshToken);

        RedisRefreshToken token = RedisRefreshToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .deviceId(deviceId)
                .parentTokenId(parentTokenId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        Duration ttl = Duration.ofSeconds(refreshExpireSeconds);

        redisTemplate.opsForValue().set(TOKEN_PREFIX + hash, token, ttl);
        redisTemplate.opsForSet().add(USER_PREFIX + userId, hash);
        redisTemplate.expire(USER_PREFIX + userId, ttl);
    }

    // =========================
    // 유효성 검사
    // =========================
    @Override
    public Optional<Long> findValidUserId(String rawRefreshToken) {

        String hash = hashUtil.hash(rawRefreshToken);

        RedisRefreshToken token = getToken(hash);

        if (token == null || token.isRevoked()) {
            return Optional.empty();
        }

        if (token.isExpired()) {
            deleteToken(token.getUserId(), hash);
            return Optional.empty();
        }

        return Optional.of(token.getUserId());
    }

    // =========================
    // 조회
    // =========================
    @Override
    public Optional<RefreshToken> findToken(String rawRefreshToken) {

        String hash = hashUtil.hash(rawRefreshToken);

        RedisRefreshToken token = getToken(hash);

        if (token == null) {
            return Optional.empty();
        }

        RefreshToken mapped = RefreshToken.builder()
                .userId(token.getUserId())
                .tokenHash(token.getTokenHash())
                .expiresAt(token.getExpiresAt())
                .deviceId(token.getDeviceId())
                .ipAddress(token.getIpAddress())
                .userAgent(token.getUserAgent())
                .parentTokenId(token.getParentTokenId())
                .revoked(token.isRevoked())
                .revokedAt(token.getRevokedAt())
                .build();

        return Optional.of(mapped);
    }

    // =========================
    // Rotation
    // =========================
    @Override
    public void rotate(
            String rawOldRefreshToken,
            String newRawRefreshToken,
            LocalDateTime newExpiresAt,
            String ipAddress,
            String userAgent,
            String deviceId
    ) {

        String oldHash = hashUtil.hash(rawOldRefreshToken);

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + oldHash, "1", Duration.ofSeconds(5));

        if (Boolean.FALSE.equals(locked)) {
            throw new RefreshTokenRevokedException();
        }

        RedisRefreshToken oldToken = getToken(oldHash);

        if (oldToken == null) {

            Boolean reused = redisTemplate.hasKey(REVOKED_PREFIX + oldHash);

            if (Boolean.TRUE.equals(reused)) {

                Long userId = (Long) redisTemplate.opsForValue()
                        .get(REVOKED_PREFIX + oldHash);

                if (userId != null) {
                    revokeAllByUserId(userId);
                }

                throw new RefreshTokenReuseDetectedException();
            }

            throw new RefreshTokenNotFoundException();
        }

        if (oldToken.isRevoked()) {
            revokeAllByUserId(oldToken.getUserId());
            throw new RefreshTokenRevokedException();
        }

        if (oldToken.isExpired()) {
            deleteToken(oldToken.getUserId(), oldHash);
            throw new RefreshTokenExpiredException();
        }

        redisTemplate.opsForValue().set(
                REVOKED_PREFIX + oldHash,
                oldToken.getUserId(),
                safeTtl(oldToken.getExpiresAt())
        );

        deleteToken(oldToken.getUserId(), oldHash);

        createAndSave(
                oldToken.getUserId(),
                newRawRefreshToken,
                newExpiresAt,
                ipAddress,
                userAgent,
                deviceId,
                oldToken.getParentTokenId()
        );
    }

    // =========================
    // revoke
    // =========================
    @Override
    public void revoke(String rawRefreshToken) {

        String hash = hashUtil.hash(rawRefreshToken);

        RedisRefreshToken token = getToken(hash);

        if (token == null) {
            return;
        }

        redisTemplate.opsForValue().set(
                REVOKED_PREFIX + hash,
                token.getUserId(),
                safeTtl(token.getExpiresAt())
        );

        deleteToken(token.getUserId(), hash);
    }

    // =========================
    // 전체 revoke
    // =========================
    @Override
    public void revokeAllByUserId(Long userId) {

        Set<Object> hashes = redisTemplate.opsForSet().members(USER_PREFIX + userId);

        if (hashes == null || hashes.isEmpty()) {
            redisTemplate.delete(USER_PREFIX + userId);
            return;
        }

        for (Object hashObj : hashes) {
            String hash = hashObj.toString();
            redisTemplate.delete(TOKEN_PREFIX + hash);
        }

        redisTemplate.delete(USER_PREFIX + userId);
    }

    // =========================
    // 디바이스 조회
    // =========================
    @Override
    public List<DeviceSessionResponse> getActiveDevices(
            Long userId,
            String currentRefreshToken
    ) {

        String userKey = USER_PREFIX + userId;

        Set<Object> hashes = redisTemplate.opsForSet().members(userKey);

        if (hashes == null || hashes.isEmpty()) {
            return List.of();
        }

        String currentTokenHash =
                (currentRefreshToken != null && !currentRefreshToken.isBlank())
                        ? hashUtil.hash(currentRefreshToken)
                        : null;

        return hashes.stream()
                .map(Object::toString)
                .map(hash -> {

                    RedisRefreshToken token = getToken(hash);

                    if (token == null || token.isRevoked() || token.isExpired()) {
                        return null;
                    }

                    return DeviceSessionResponse.builder()
                            .deviceId(token.getDeviceId())
                            .userAgent(token.getUserAgent())
                            .ipAddress(token.getIpAddress())
                            .createdAt(token.getCreatedAt())
                            .expiresAt(token.getExpiresAt())
                            .current(currentTokenHash != null && currentTokenHash.equals(hash))
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // =========================
    // 디바이스 revoke
    // =========================
    @Override
    public void revokeByUserIdAndDeviceId(Long userId, String deviceId) {

        String userKey = USER_PREFIX + userId;

        Set<Object> hashes = redisTemplate.opsForSet().members(userKey);

        if (hashes == null || hashes.isEmpty()) {
            return;
        }

        for (Object hashObj : hashes) {

            String hash = hashObj.toString();

            RedisRefreshToken token = getToken(hash);

            if (token == null) {
                continue;
            }

            // 🔥 NPE 방지
            if (token.getDeviceId() == null || !token.getDeviceId().equals(deviceId)) {
                continue;
            }

            redisTemplate.opsForValue().set(
                    REVOKED_PREFIX + hash,
                    token.getUserId(),
                    safeTtl(token.getExpiresAt())
            );

            deleteToken(userId, hash);
        }
    }

    // =========================
    // 내부 util
    // =========================
    private RedisRefreshToken getToken(String hash) {

        Object value = redisTemplate.opsForValue().get(TOKEN_PREFIX + hash);

        if (value == null) return null;

        if (!(value instanceof RedisRefreshToken)) {
            throw new IllegalStateException("Invalid Redis token type");
        }

        return (RedisRefreshToken) value;
    }

    private void deleteToken(Long userId, String hash) {
        redisTemplate.delete(TOKEN_PREFIX + hash);
        redisTemplate.opsForSet().remove(USER_PREFIX + userId, hash);
    }

    private Duration safeTtl(LocalDateTime expiresAt) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        return ttl.isNegative() || ttl.isZero()
                ? Duration.ofSeconds(1)
                : ttl;
    }
}