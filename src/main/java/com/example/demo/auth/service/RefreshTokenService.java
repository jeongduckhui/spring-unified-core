package com.example.demo.auth.service;

import com.example.demo.auth.dto.DeviceSessionResponse;
import com.example.demo.token.domain.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 관리 서비스 인터페이스
 *
 * 이 인터페이스는 RefreshToken 저장 및 관리 기능을 정의한다.
 *
 * 설계 목적
 *
 * RefreshToken 저장 방식을 유연하게 교체할 수 있도록 하기 위해
 * 인터페이스 기반으로 설계하였다.
 *
 * 구현체 예
 *
 * RefreshTokenDbService
 * - DB 기반 RefreshToken 관리
 *
 * RefreshTokenRedisService
 * - Redis 기반 RefreshToken 관리
 *
 * Spring Profile을 통해 구현체를 선택한다.
 *
 * 예
 *
 * application.yml
 *
 * spring.profiles.active=db
 * 또는
 * spring.profiles.active=redis
 *
 * 이렇게 하면 동일한 AuthService 코드로
 * DB / Redis 저장 방식을 교체할 수 있다.
 *
 * 주요 기능
 *
 * RefreshToken 생성 및 저장
 * RefreshToken 조회
 * RefreshToken Rotation
 * RefreshToken revoke
 * 사용자 전체 세션 revoke
 * 디바이스 세션 조회
 * 특정 디바이스 세션 종료
 */
public interface RefreshTokenService {

    /**
     * RefreshToken 생성 및 저장
     *
     * 호출 시점
     *
     * OAuth2 로그인 성공
     * 또는
     * RefreshToken Rotation 이후
     *
     * 저장되는 정보
     *
     * userId
     * tokenHash
     * expiresAt
     * deviceId
     * ipAddress
     * userAgent
     * parentTokenId
     *
     * parentTokenId는 RefreshToken Rotation 관계를 추적하기 위한 필드이다.
     */
    void createAndSave(
            Long userId,
            String rawRefreshToken,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent,
            String deviceId,
            Long parentTokenId
    );

    /**
     * RefreshToken이 유효한지 검사하고 userId 반환
     *
     * 검증 조건
     *
     * 토큰 존재
     * revoked 아님
     * 만료되지 않음
     *
     * 사용 목적
     *
     * RefreshToken 기반 사용자 인증
     */
    Optional<Long> findValidUserId(String rawRefreshToken);

    /**
     * RefreshToken 조회
     *
     * Rotation 과정에서 기존 토큰을 조회하기 위해 사용된다.
     *
     * raw token을 전달하면
     * 내부에서 hash로 변환 후 조회한다.
     */
    Optional<RefreshToken> findToken(String rawRefreshToken);

    /**
     * RefreshToken Rotation
     *
     * RefreshToken을 재사용하지 못하도록
     * 새로운 토큰을 발급하는 구조이다.
     *
     * 처리 과정
     *
     * 기존 RefreshToken 검증
     * 기존 RefreshToken revoke
     * 새로운 RefreshToken 생성
     * parentTokenId 설정
     *
     * 보안 목적
     *
     * RefreshToken 탈취 공격 방지
     */
    void rotate(
            String rawOldRefreshToken,
            String newRawRefreshToken,
            LocalDateTime newExpiresAt,
            String ipAddress,
            String userAgent,
            String deviceId
    );

    /**
     * 특정 RefreshToken revoke
     *
     * 호출 시점
     *
     * 로그아웃
     */
    void revoke(String rawRefreshToken);

    /**
     * 사용자 전체 RefreshToken revoke
     *
     * 호출 시점
     *
     * RefreshToken Reuse Attack 탐지
     *
     * 예
     *
     * 이미 revoke된 RefreshToken 사용 시도
     *
     * 이 경우
     * 해당 사용자의 모든 세션을 종료한다.
     */
    void revokeAllByUserId(Long userId);

    /**
     * 사용자 활성 디바이스 목록 조회
     *
     * 프론트에서
     *
     * "로그인된 기기 목록"
     *
     * 기능 구현 시 사용된다.
     *
     * 반환 정보
     *
     * deviceId
     * userAgent
     * ipAddress
     * 생성 시간
     * 만료 시간
     * 현재 세션 여부
     */
    List<DeviceSessionResponse> getActiveDevices(
            Long userId,
            String currentRefreshToken
    );

    /**
     * 특정 디바이스 세션 종료
     *
     * 예
     *
     * 사용자가
     *
     * "다른 기기에서 로그아웃"
     *
     * 기능을 수행할 때 호출된다.
     */
    void revokeByUserIdAndDeviceId(
            Long userId,
            String deviceId
    );
}