package com.example.demo.user.repository;

import com.example.demo.user.domain.RoleHierarchyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RoleHierarchyEntity Repository
 *
 * 권한 계층(RoleHierarchy)을 관리하는 JPA Repository이다.
 *
 * RoleHierarchy는 권한 간의 상속 관계를 표현한다.
 *
 * 예
 *
 * ROLE_ADMIN > ROLE_USER
 *
 * 즉
 *
 * ADMIN 권한을 가진 사용자는
 * USER 권한도 자동으로 가진다.
 *
 * 이 Repository는
 *
 * - 권한 계층 저장
 * - 권한 계층 조회
 * - 권한 계층 존재 여부 확인
 *
 * 등에 사용된다.
 */
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchyEntity, Long> {

    /**
     * 특정 권한 계층이 이미 존재하는지 확인
     *
     * 예
     *
     * parentRole = ROLE_ADMIN
     * childRole = ROLE_USER
     *
     * 이미 존재하면 true
     * 존재하지 않으면 false
     *
     * 주 사용처
     *
     * RoleDataInitializer
     *
     * 서버 시작 시
     * ROLE_ADMIN > ROLE_USER
     * 계층이 이미 존재하는지 검사하기 위해 사용한다.
     *
     * Spring Data JPA가 메서드 이름을 기반으로
     * 자동으로 쿼리를 생성한다.
     *
     * 생성되는 SQL 예
     *
     * SELECT COUNT(*)
     * FROM role_hierarchy
     * WHERE parent_role = ?
     *   AND child_role = ?
     */
    boolean existsByParentRoleAndChildRole(String parentRole, String childRole);
}