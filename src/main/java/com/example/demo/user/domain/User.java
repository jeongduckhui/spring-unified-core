package com.example.demo.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 *
 * OAuth2 / OIDC 로그인 사용자 계정을 저장하는 테이블이다.
 *
 * 현재 프로젝트에서는
 *
 * Google OAuth2
 * Keycloak OIDC
 *
 * 로그인 사용자를 이 테이블에 저장한다.
 *
 * 사용자 식별 방식
 *
 * provider + provider_subject
 *
 * 예
 *
 * provider = GOOGLE
 * provider_subject = 109384750293847
 *
 * 즉
 *
 * "외부 인증 서버 사용자 식별값"
 * 을 내부 사용자 계정과 연결한다.
 *
 * 이 테이블은 다음 기능에서 사용된다.
 *
 * OAuth2 로그인 사용자 저장
 * JWT 사용자 식별
 * RBAC 권한 관리
 * 사용자 프로필 관리
 */
@Entity

/**
 * DB 테이블 이름
 */
@Table(
        name = "users",

        /**
         * 유니크 제약 조건
        */
        uniqueConstraints = {

                /**
                 * 동일 provider + provider_subject
                 * 중복 저장 방지
                 *
                 * 즉 동일 OAuth 계정 중복 가입 방지
                 */
                @UniqueConstraint(columnNames = {"provider", "provider_subject"}),

                /**
                 * 이메일 중복 방지
                 */
                @UniqueConstraint(columnNames = {"email"})
        }
)

@Getter

/**
 * JPA 기본 생성자
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
public class User {

    /**
     * PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OAuth 제공자
     *
     * 예
     *
     * GOOGLE
     * KEYCLOAK
     */
    @Column(nullable = false)
    private String provider;

    /**
     * OAuth / OIDC 사용자 고유 식별자
     *
     * OIDC의 sub claim 값이다.
     *
     * 예
     *
     * sub = 103847503948573
     */
    @Column(name = "provider_subject", nullable = false)
    private String providerSubject;

    /**
     * 사용자 이메일
     */
    @Column(nullable = false)
    private String email;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 상태
     *
     * 예
     *
     * ACTIVE
     * BLOCKED
     * DELETED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /**
     * 계정 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 계정 수정 시간
     */
    private LocalDateTime updatedAt;

    /**
     * 사용자 권한 목록
     *
     * User
     *  ↓
     * UserRole
     *  ↓
     * Role
     *
     * 구조의 중간 매핑 테이블
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    /**
     * INSERT 전에 자동 실행
     */
    @PrePersist
    public void prePersist() {

        /**
         * 생성 시간 설정
         */
        this.createdAt = LocalDateTime.now();

        /**
         * 수정 시간 설정
         */
        this.updatedAt = LocalDateTime.now();

        /**
         * 기본 상태 설정
         */
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    /**
     * UPDATE 전에 자동 실행
     */
    @PreUpdate
    public void preUpdate() {

        /**
         * 수정 시간 갱신
         */
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 프로필 업데이트
     *
     * OAuth 로그인 시
     * 이메일 / 이름 변경 반영
     */
    public void updateProfile(String email, String name) {

        this.email = email;
        this.name = name;

        /**
         * 로그인 성공 시 상태 활성화
         */
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 사용자에게 권한 추가
     */
    public void addRole(Role role) {

        /**
         * role null 방지
         */
        if (role == null) {
            return;
        }

        /**
         * userRoles 리스트 null 방지
         */
        if (this.userRoles == null) {
            this.userRoles = new ArrayList<>();
        }

        /**
         * 이미 존재하는 권한인지 검사
         */
        boolean alreadyExists = this.userRoles.stream()
                .anyMatch(userRole ->
                        userRole.getRole().getRoleName().equals(role.getRoleName())
                );

        /**
         * 이미 존재하면 추가하지 않음
         */
        if (alreadyExists) {
            return;
        }

        /**
         * UserRole 생성
         */
        UserRole userRole = UserRole.builder()
                .user(this)
                .role(role)
                .build();

        /**
         * 사용자 권한 목록에 추가
         */
        this.userRoles.add(userRole);
    }

    /**
     * 사용자 권한 제거
     */
    public void removeRole(Role role) {

        /**
         * 해당 권한 제거
         */
        this.userRoles.removeIf(userRole ->
                userRole.getRole().getRoleName().equals(role.getRoleName())
        );
    }
}