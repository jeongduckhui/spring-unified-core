package com.example.demo.auth.service;

import com.example.demo.auth.dto.DeviceSessionResponse;
import com.example.demo.auth.exception.RefreshTokenExpiredException;
import com.example.demo.auth.exception.RefreshTokenNotFoundException;
import com.example.demo.auth.exception.RefreshTokenRevokedException;
import com.example.demo.token.domain.RefreshToken;
import com.example.demo.token.repository.RefreshTokenRepository;
import com.example.demo.token.util.RefreshTokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 관리 서비스 (DB 저장 방식)
 *
 * RefreshTokenService 인터페이스의 DB 구현체이다.
 *
 * 이 서비스는 다음 기능을 담당한다.
 *
 * RefreshToken 생성 및 저장
 * RefreshToken 조회
 * RefreshToken Rotation 처리
 * RefreshToken revoke 처리
 * 사용자 전체 세션 revoke
 * 디바이스 세션 조회
 * 특정 디바이스 세션 종료
 *
 * Profile("db") 설정이 되어 있기 때문에
 *
 * application 실행 시
 *
 * spring.profiles.active=db
 *
 * 로 설정된 경우에만 활성화된다.
 *
 * Redis 기반 구현체와 교체 가능하도록 설계된 구조이다.
 */
@Service
@ConditionalOnProperty(
        name = "app.refresh-token.store",
        havingValue = "db",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Transactional
public class RefreshTokenDbService implements RefreshTokenService {

    /**
     * RefreshToken DB 접근 Repository
     *
     * RefreshToken 테이블에 대한 CRUD 작업 수행
     */
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * RefreshToken 해시 유틸
     *
     * RefreshToken을 평문으로 저장하지 않고
     * 해시값으로 저장하기 위해 사용된다.
     *
     * 보안 이유
     *
     * DB 유출 시 토큰 탈취 방지
     */
    private final RefreshTokenHashUtil refreshTokenHashUtil;


    /**
     * RefreshToken 생성 및 저장
     *
     * 로그인 성공 시 호출된다.
     */
    @Override
    public void createAndSave(Long userId,
                              String rawRefreshToken,
                              LocalDateTime expiresAt,
                              String ipAddress,
                              String userAgent,
                              String deviceId,
                              Long parentTokenId) {

        /**
         * RefreshToken을 해시 처리
         *
         * DB에는 raw token이 아니라
         * hash 값만 저장한다.
         */
        String tokenHash = refreshTokenHashUtil.hash(rawRefreshToken);

        /**
         * RefreshToken Entity 생성
         */
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(deviceId)
                .parentTokenId(parentTokenId)
                .build();

        /**
         * DB 저장
         */
        refreshTokenRepository.save(refreshToken);
    }


    /**
     * 유효한 RefreshToken인지 검사 후 userId 반환
     *
     * 주로 인증 검증 로직에서 사용된다.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findValidUserId(String rawRefreshToken) {

        /**
         * raw token을 hash로 변환
         */
        String tokenHash = refreshTokenHashUtil.hash(rawRefreshToken);

        /**
         * DB 조회 후
         *
         * 만료되지 않은 토큰
         * revoke되지 않은 토큰
         *
         * 인 경우 userId 반환
         */
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> !token.isExpired())
                .filter(token -> !token.isRevoked())
                .map(RefreshToken::getUserId);
    }


    /**
     * RefreshToken 조회
     *
     * Rotation 또는 검증 로직에서 사용된다.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findToken(String rawRefreshToken) {

        /**
         * token hash 생성
         */
        String tokenHash = refreshTokenHashUtil.hash(rawRefreshToken);

        /**
         * DB 조회
         */
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }


    /**
     * RefreshToken Rotation 처리
     *
     * 기존 RefreshToken 사용 시
     * 새로운 RefreshToken을 발급하고
     * 기존 토큰을 revoke한다.
     */
    @Override
    public void rotate(String rawOldRefreshToken,
                       String newRawRefreshToken,
                       LocalDateTime newExpiresAt,
                       String ipAddress,
                       String userAgent,
                       String deviceId) {

        /**
         * 기존 토큰 hash 생성
         */
        String oldTokenHash = refreshTokenHashUtil.hash(rawOldRefreshToken);

        /**
         * 기존 토큰 조회
         */
        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(oldTokenHash)
                .orElseThrow(RefreshTokenNotFoundException::new);

        /**
         * 이미 revoke된 토큰이면
         * Reuse 공격 가능성이 있다.
         */
        if (oldToken.isRevoked()) {

            /**
             * 사용자 전체 세션 revoke
             */
            revokeAllByUserId(oldToken.getUserId());

            throw new RefreshTokenRevokedException();
        }

        /**
         * 토큰 만료 검사
         */
        if (oldToken.isExpired()) {

            /**
             * 만료된 토큰 revoke 처리
             */
            oldToken.revoke();

            throw new RefreshTokenExpiredException();
        }

        /**
         * 기존 토큰 revoke
         */
        oldToken.revoke();

        /**
         * 새로운 RefreshToken 생성
         */
        RefreshToken newToken = RefreshToken.builder()
                .userId(oldToken.getUserId())
                .tokenHash(refreshTokenHashUtil.hash(newRawRefreshToken))
                .expiresAt(newExpiresAt)
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceId(deviceId)
                .parentTokenId(oldToken.getId())
                .build();

        /**
         * 새 토큰 저장
         */
        refreshTokenRepository.save(newToken);
    }


    /**
     * 특정 RefreshToken revoke
     *
     * 로그아웃 시 사용된다.
     */
    @Override
    public void revoke(String rawRefreshToken) {

        /**
         * token hash 생성
         */
        String tokenHash = refreshTokenHashUtil.hash(rawRefreshToken);

        /**
         * DB 조회 후 revoke
         */
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(RefreshToken::revoke);
    }


    /**
     * 사용자 전체 RefreshToken revoke
     *
     * Reuse Attack 발생 시 사용된다.
     */
    @Override
    public void revokeAllByUserId(Long userId) {

        refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
                .forEach(RefreshToken::revoke);
    }


    /**
     * 사용자 활성 디바이스 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceSessionResponse> getActiveDevices(Long userId, String currentRefreshToken) {

        /**
         * 현재 사용 중인 refreshToken hash 계산
         */
        String currentTokenHash =
                (currentRefreshToken != null && !currentRefreshToken.isBlank())
                        ? refreshTokenHashUtil.hash(currentRefreshToken)
                        : null;

        /**
         * 사용자 활성 토큰 조회
         */
        return refreshTokenRepository
                .findAllByUserIdAndRevokedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(token -> DeviceSessionResponse.builder()
                        .deviceId(token.getDeviceId())
                        .userAgent(token.getUserAgent())
                        .ipAddress(token.getIpAddress())
                        .current(currentTokenHash != null && currentTokenHash.equals(token.getTokenHash()))
                        .createdAt(token.getCreatedAt())
                        .expiresAt(token.getExpiresAt())
                        .build())
                .toList();
    }


    /**
     * 특정 디바이스 세션 로그아웃
     *
     * 해당 디바이스의 모든 RefreshToken revoke
     */
    @Override
    public void revokeByUserIdAndDeviceId(Long userId, String deviceId) {

        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUserIdAndDeviceIdAndRevokedFalse(userId, deviceId);

        tokens.forEach(RefreshToken::revoke);
    }
}