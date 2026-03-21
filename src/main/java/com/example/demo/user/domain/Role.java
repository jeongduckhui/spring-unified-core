package com.example.demo.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role 엔티티
 *
 * 시스템에서 사용할 "권한 종류"를 정의하는 테이블이다.
 *
 * 예
 *
 * ROLE_USER
 * ROLE_ADMIN
 *
 * 즉 사용자에게 부여되는 권한의 "정의 테이블"이다.
 *
 * 현재 프로젝트의 RBAC 구조
 *
 * User
 *   ↓
 * UserRole
 *   ↓
 * Role
 *
 * 구조에서
 *
 * Role은 실제 권한 이름을 보관하는 기준 테이블 역할을 한다.
 *
 * 이 엔티티는 다음 기능에서 사용된다.
 *
 * Spring Security 권한 목록 생성
 * JWT roles claim 생성
 * 기본 ROLE_USER 부여
 * RoleHierarchy 구성
 */
@Entity

/**
 * DB 테이블 이름 설정
 *
 * 테이블명
 * role
 */
@Table(
        name = "role",

        /**
         * role_name은 유일해야 한다.
         *
         * 예
         *
         * ROLE_USER 중복 저장 금지
         * ROLE_ADMIN 중복 저장 금지
        */
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "role_name")
        }
)

@Getter

/**
 * JPA 기본 생성자
 *
 * protected로 제한하여
 * 외부에서 무분별하게 생성하지 못하게 한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 전체 필드 생성자
 */
@AllArgsConstructor

/**
 * Builder 패턴 지원
 *
 * 객체 생성 시 가독성을 높인다.
 */
@Builder
public class Role {

    /**
     * PK
     *
     * role 테이블 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 권한명
     *
     * Spring Security에서는 일반적으로
     * ROLE_ 접두사를 포함한 권한명을 사용한다.
     *
     * 예
     *
     * ROLE_USER
     * ROLE_ADMIN
     *
     * 이 값은 이후
     *
     * GrantedAuthority
     * JWT roles claim
     * @PreAuthorize
     *
     * 등에 사용된다.
     */
    @Column(name = "role_name", nullable = false)
    private String roleName; // ROLE_USER, ROLE_ADMIN

    /**
     * 권한 설명
     *
     * 운영자 또는 개발자가
     * 해당 권한의 의미를 이해하기 쉽게 하기 위한 설명 필드이다.
     *
     * 예
     *
     * 일반 사용자 권한
     * 관리자 권한
     */
    private String description;
}