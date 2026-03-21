package com.example.demo.token.repository;

import com.example.demo.token.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken DB Repository
 *
 * RefreshToken 엔티티에 대한 DB 접근을 담당한다.
 *
 * Spring Data JPA의 JpaRepository를 상속하여
 * 기본 CRUD 기능을 제공받는다.
 *
 * 제공되는 기본 기능 예
 *
 * save()
 * findById()
 * delete()
 * findAll()
 *
 * 이 인터페이스에서는
 * RefreshToken 관리에 필요한 추가 조회 메서드를 정의한다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * tokenHash로 RefreshToken 조회
     *
     * RefreshToken 원문은 DB에 저장하지 않기 때문에
     * hash 값을 기준으로 조회한다.
     *
     * 사용 위치
     *
     * - RefreshToken 검증
     * - RefreshToken rotation
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * 특정 사용자 모든 RefreshToken 조회
     *
     * 사용 예
     *
     * - 사용자 전체 로그인 세션 조회
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * 특정 사용자 활성 RefreshToken 조회
     *
     * revoked = false 조건
     *
     * 즉 아직 유효한 토큰들만 조회한다.
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    /**
     * 특정 사용자 + 특정 deviceId 토큰 조회
     *
     * revoked = false 조건
     *
     * 사용 예
     *
     * 특정 디바이스 로그인 세션 조회
     */
    Optional<RefreshToken> findByUserIdAndDeviceIdAndRevokedFalse(Long userId, String deviceId);

    /**
     * 사용자 활성 세션 목록 조회
     *
     * 생성시간 기준 내림차순 정렬
     *
     * 사용 위치
     *
     * /auth/devices API
     */
    List<RefreshToken> findAllByUserIdAndRevokedFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자 + 특정 deviceId 토큰 조회
     *
     * revoked = false 조건
     *
     * 사용 위치
     *
     * 특정 디바이스 로그아웃
     */
    List<RefreshToken> findAllByUserIdAndDeviceIdAndRevokedFalse(Long userId, String deviceId);

    /**
     * 만료된 RefreshToken 삭제
     *
     * expiresAt < now 조건
     *
     * 반환값
     *
     * 삭제된 row 수
     *
     * 사용 위치
     *
     * RefreshTokenCleanupScheduler
     */
    long deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * revoke된 토큰 중 일정 기간 지난 토큰 삭제
     *
     * revoked = true
     * revokedAt < cutoff
     *
     * 반환값
     *
     * 삭제된 row 수
     *
     * 사용 위치
     *
     * RefreshTokenCleanupScheduler
     */
    long deleteByRevokedTrueAndRevokedAtBefore(LocalDateTime cutoff);
}