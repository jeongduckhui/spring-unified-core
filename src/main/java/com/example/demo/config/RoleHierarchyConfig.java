package com.example.demo.config;

import com.example.demo.user.domain.RoleHierarchyEntity;
import com.example.demo.user.repository.RoleHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role Hierarchy 설정 클래스
 *
 * Spring Security에서 권한 계층(Role Hierarchy)을 설정하는 Bean을 생성한다.
 *
 * 권한 계층이란?
 *
 * 예:
 *
 * ROLE_ADMIN > ROLE_USER
 *
 * 의미
 *
 * ROLE_ADMIN 권한을 가진 사용자는
 * 자동으로 ROLE_USER 권한도 가진 것으로 간주된다.
 *
 * 즉,
 *
 * ADMIN → USER 권한 자동 포함
 *
 *
 * 이 프로젝트에서는
 * RoleHierarchy 정보를 DB에서 읽어와
 * 동적으로 RoleHierarchy를 생성하도록 설계되어 있다.
 *
 * 장점
 *
 * 1. 코드 수정 없이 DB로 권한 계층 변경 가능
 * 2. 운영 중에도 권한 구조 변경 가능
 * 3. 대규모 시스템에서 권한 관리 유연성 확보
 */
@Configuration
@RequiredArgsConstructor
public class RoleHierarchyConfig {

    /**
     * RoleHierarchy 정보를 조회하기 위한 Repository
     *
     * DB 테이블 예시
     *
     * role_hierarchy
     *
     * parent_role | child_role
     * ------------------------
     * ROLE_ADMIN  | ROLE_USER
     * ROLE_ADMIN  | ROLE_MANAGER
     *
     * 이 정보를 읽어서 Spring Security RoleHierarchy 구조를 생성한다.
     */
    private final RoleHierarchyRepository roleHierarchyRepository;


    /**
     * RoleHierarchy Bean 생성
     *
     * Spring Security는 이 Bean을 자동으로 감지하여
     * 권한 검사 시 계층 구조를 적용한다.
     *
     * 예:
     *
     * @PreAuthorize("hasRole('USER')")
     *
     * 실제 권한이
     *
     * ROLE_ADMIN 인 경우에도 통과한다.
     *
     * 이유
     *
     * ROLE_ADMIN > ROLE_USER
     */
    @Bean
    public RoleHierarchy roleHierarchy() {

        // ============================
        // DB에서 RoleHierarchy 조회
        // ============================

        /**
         * DB에서 모든 RoleHierarchyEntity 조회
         *
         * 예:
         *
         * ROLE_ADMIN > ROLE_USER
         * ROLE_ADMIN > ROLE_MANAGER
         */
        List<RoleHierarchyEntity> hierarchies = roleHierarchyRepository.findAll();


        // ============================
        // Spring Security 형식으로 변환
        // ============================

        /**
         * Spring Security RoleHierarchy 문자열 형식
         *
         * 예:
         *
         * ROLE_ADMIN > ROLE_USER
         * ROLE_MANAGER > ROLE_USER
         *
         * 줄 단위로 계층 정의
         *
         * 즉,
         *
         * parentRole > childRole
         *
         * 구조로 문자열을 만들어야 한다.
         */
        String hierachy =  hierarchies.stream()

                /**
                 * DB Entity → RoleHierarchy 문자열 변환
                 *
                 * 예:
                 *
                 * parentRole = ROLE_ADMIN
                 * childRole  = ROLE_USER
                 *
                 * 결과
                 *
                 * ROLE_ADMIN > ROLE_USER
                 */
                .map(h -> h.getParentRole() + ">" + h.getChildRole())

                /**
                 * 여러 줄로 연결
                 *
                 * 예:
                 *
                 * ROLE_ADMIN>ROLE_USER
                 * ROLE_MANAGER>ROLE_USER
                 */
                .collect(Collectors.joining("\n"));


        // ============================
        // Spring Security RoleHierarchy 객체 생성
        // ============================

        /**
         * Spring Security 기본 RoleHierarchy 구현체
         */
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();


        /**
         * 계층 구조 설정
         *
         * 예:
         *
         * ROLE_ADMIN > ROLE_USER
         * ROLE_MANAGER > ROLE_USER
         */
        roleHierarchy.setHierarchy(hierachy);


        // Spring Security에서 사용할 RoleHierarchy Bean 반환
        return roleHierarchy;
    }
}