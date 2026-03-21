package com.example.demo.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * UserRole 엔티티
 *
 * 사용자와 권한(Role)을 연결하는 매핑 테이블이다.
 *
 * RBAC(Role-Based Access Control) 구조에서
 * 사용자와 권한은 N:M 관계를 가진다.
 *
 * 예
 *
 * User
 *  id = 1
 *
 * Role
 *  ROLE_USER
 *  ROLE_ADMIN
 *
 * 즉
 *
 * 한 사용자는 여러 권한을 가질 수 있고
 * 하나의 권한은 여러 사용자에게 부여될 수 있다.
 *
 * 이 N:M 관계를 해결하기 위해
 *
 * UserRole
 *
 * 중간 테이블을 사용한다.
 */
@Entity

/**
 * DB 테이블 이름
 */
@Table(
        name = "user_role",

        /**
         * 동일 사용자에게 동일 권한 중복 부여 방지
         *
         * 예
         *
         * user_id = 1
         * role_id = ROLE_USER
         *
         * 이 데이터가 이미 있으면
         * 다시 저장되지 않도록 제한한다.
        */
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "role_id"})
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
public class UserRole {

    /**
     * PK
     *
     * user_role 테이블 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 엔티티
     *
     * UserRole
     *   ↓
     * User
     *
     * 관계
     *
     * Many UserRole → One User
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )

    /**
     * user 테이블 FK
     */
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 권한 엔티티
     *
     * UserRole
     *   ↓
     * Role
     *
     * 관계
     *
     * Many UserRole → One Role
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )

    /**
     * role 테이블 FK
     */
    @JoinColumn(name = "role_id")
    private Role role;
}