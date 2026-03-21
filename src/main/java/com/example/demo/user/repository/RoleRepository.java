package com.example.demo.user.repository;

import com.example.demo.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Role Repository
 *
 * 권한(Role) 엔티티를 관리하는 JPA Repository이다.
 *
 * Role 테이블은 시스템 권한 정보를 저장한다.
 *
 * 예
 *
 * ROLE_USER
 * ROLE_ADMIN
 *
 * 이 Repository는 다음 기능을 수행한다.
 *
 * - 권한 조회
 * - 권한 저장
 * - 권한 존재 여부 확인
 *
 * JpaRepository를 상속하기 때문에
 * 기본 CRUD 기능도 자동으로 제공된다.
 *
 * 제공되는 기본 메서드 예
 *
 * save()
 * findById()
 * findAll()
 * delete()
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * role_name으로 권한 조회
     *
     * 예
     *
     * ROLE_USER
     * ROLE_ADMIN
     *
     * Spring Data JPA가 메서드 이름을 기반으로
     * 자동으로 쿼리를 생성한다.
     *
     * 생성되는 SQL 예
     *
     * SELECT *
     * FROM role
     * WHERE role_name = ?
     *
     * 반환값
     *
     * Optional<Role>
     *
     * 이유
     *
     * 권한이 존재하지 않을 수 있기 때문에
     * Optional로 감싸서 반환한다.
     *
     * 사용 예
     *
     * roleRepository.findByRoleName("ROLE_USER")
     */
    Optional<Role> findByRoleName(String roleName);

}