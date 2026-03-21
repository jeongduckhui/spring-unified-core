package com.example.demo.token.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RefreshToken 엔티티
 *
 * 이 테이블은 사용자의 로그인 세션을 관리하기 위한 테이블이다.
 *
 * 현재 프로젝트는
 *
 * AccessToken  → Stateless JWT
 * RefreshToken → DB 기반 세션 관리
 *
 * 구조를 사용한다.
 *
 * 즉 RefreshToken이 실제 "로그인 세션" 역할을 한다.
 *
 * 주요 기능
 *
 * 1 RefreshToken 저장
 * 2 RefreshToken Rotation 관리
 * 3 Device 기반 로그인 세션 관리
 * 4 토큰 revoke 관리
 * 5 RefreshToken reuse attack 탐지
 */
@Entity

/**
 * DB 테이블 이름
 */
@Table(
        name = "refresh_token",

        /**
         * token_hash는 유일해야 한다.
         *
         * 동일 토큰 중복 저장 방지
        */
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "token_hash")
        }
)

@Getter

/**
 * JPA 기본 생성자
 *
 * protected로 제한하여
 * 외부에서 직접 생성하지 못하게 한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 전체 필드 생성자
 */
@AllArgsConstructor

/**
 * Builder 패턴 지원
 */
@Builder
public class RefreshToken {

    /**
     * PK
     *
     * refresh_token 테이블 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 사용자 토큰인지
     *
     * FK처럼 사용하지만
     * 단순 ID로 저장한다.
     *
     * 이유
     *
     * - 성능
     * - JPA Lazy 문제 방지
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * RefreshToken hash
     *
     * 보안상 원문 토큰은 저장하지 않는다.
     *
     * 이유
     *
     * DB 탈취 시 토큰 악용 방지
     *
     * 저장 방식
     *
     * hash(refreshToken)
     */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /**
     * RefreshToken 만료 시간
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * RefreshToken 생성 시간
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 revoke 여부
     *
     * true  → 사용 불가
     * false → 정상 토큰
     */
    @Column(nullable = false)
    private boolean revoked;

    /**
     * 토큰 revoke된 시간
     */
    private LocalDateTime revokedAt;

    /**
     * 로그인 IP
     *
     * 보안 감사 / 이상 로그인 탐지 용도
     */
    private String ipAddress;

    /**
     * 브라우저 / 기기 정보
     *
     * 예
     *
     * Chrome / Windows
     * Safari / iPhone
     */
    private String userAgent;

    // ===== 추가되는 실무 필드 =====

    /**
     * Device ID
     *
     * 브라우저 / 앱 단위 세션 관리
     *
     * 예
     *
     * 사용자 로그인
     *
     * PC
     * 모바일
     * 태블릿
     *
     * 각각 다른 deviceId로 관리된다.
     */
    @Column(name = "device_id", length = 100)
    private String deviceId;

    /**
     * RefreshToken Rotation 추적
     *
     * RefreshToken이 갱신될 때
     *
     * old → new
     *
     * 관계를 기록한다.
     *
     * 예
     *
     * token1
     *   ↓
     * token2
     *   ↓
     * token3
     */
    @Column(name = "parent_token_id")
    private Long parentTokenId;

    /**
     * JPA insert 전에 자동 실행
     *
     * 생성 시 기본값 설정
     */
    @PrePersist
    public void prePersist() {

        /**
         * 생성 시간 기록
         */
        this.createdAt = LocalDateTime.now();

        /**
         * 기본 상태
         *
         * revoke되지 않은 상태
         */
        this.revoked = false;
    }

    /**
     * RefreshToken revoke 처리
     *
     * 로그아웃
     * token rotation
     * reuse attack
     *
     * 상황에서 호출된다.
     */
    public void revoke() {

        /**
         * revoke 상태 변경
         */
        this.revoked = true;

        /**
         * revoke 시간 기록
         */
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 토큰 만료 여부 검사
     */
    public boolean isExpired() {

        /**
         * expiresAt < 현재시간
         */
        return expiresAt.isBefore(LocalDateTime.now());
    }
}