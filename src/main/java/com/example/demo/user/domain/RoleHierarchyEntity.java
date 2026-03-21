package com.example.demo.user.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * RoleHierarchy 엔티티
 *
 * Spring Security의 RoleHierarchy 기능을
 * DB 기반으로 관리하기 위한 테이블이다.
 *
 * RoleHierarchy는
 *
 * 상위 권한이 하위 권한을 자동으로 포함하도록 만드는 기능이다.
 *
 * 예
 *
 * ROLE_ADMIN > ROLE_USER
 *
 * 이 경우
 *
 * ROLE_ADMIN 사용자는
 * ROLE_USER 권한도 자동으로 가진다.
 *
 * 즉 아래 권한 검사도 통과한다.
 *
 * hasRole("USER")
 *
 * 이 테이블은 그 관계를 저장한다.
 */
@Entity

/**
 * DB 테이블 이름
 */
@Table(name = "role_hierarchy")

@Getter

/**
 * JPA 기본 생성자
 *
 * protected로 제한하여
 * 외부에서 무분별하게 객체를 생성하지 못하게 한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 전체 필드 생성자
 */
@AllArgsConstructor

/**
 * Builder 패턴 지원
 *
 * 테스트 데이터 생성이나
 * 초기 데이터 등록 시 유용하다.
 */
@Builder
public class RoleHierarchyEntity {

    /**
     * PK
     *
     * role_hierarchy 테이블 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상위 권한
     *
     * 예
     *
     * ROLE_ADMIN
     */
    @Column(name = "parent_role", nullable = false)
    private String parentRole;

    /**
     * 하위 권한
     *
     * 예
     *
     * ROLE_USER
     *
     * 즉
     *
     * parentRole > childRole
     */
    @Column(name = "child_role", nullable = false)
    private String childRole;
}