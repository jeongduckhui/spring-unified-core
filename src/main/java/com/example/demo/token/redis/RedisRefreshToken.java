package com.example.demo.token.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Redis 기반 RefreshToken 저장 모델
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 과거 Redis 데이터 호환 (핵심)
@JsonIgnoreProperties(ignoreUnknown = true)

public class RedisRefreshToken implements Serializable {

    private Long userId;

    private String tokenHash;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private boolean revoked;

    private LocalDateTime revokedAt;

    private String deviceId;

    private Long parentTokenId;

    private String ipAddress;

    private String userAgent;

    /**
     * 만료 여부
     */
    public boolean isExpired() {
        return expiresAt != null
                && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * revoke 처리
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}