package com.example.demo.user.init;

import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.RoleHierarchyEntity;
import com.example.demo.user.repository.RoleHierarchyRepository;
import com.example.demo.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Role 데이터 초기화 클래스
 *
 * 애플리케이션 시작 시
 *
 * 1. 기본 권한(Role) 생성
 * 2. 권한 계층(RoleHierarchy) 생성
 *
 * 을 자동으로 수행한다.
 *
 * 예
 *
 * ROLE_ADMIN
 * ROLE_USER
 *
 * 그리고 권한 계층
 *
 * ROLE_ADMIN > ROLE_USER
 *
 * 즉
 *
 * ADMIN 권한을 가진 사용자는
 * USER 권한도 자동으로 포함된다.
 *
 * 이 초기화는 서버 시작 시 한 번 실행된다.
 */
@Configuration
@RequiredArgsConstructor
public class RoleDataInitializer {

    /**
     * Role 테이블 접근 Repository
     */
    private final RoleRepository roleRepository;

    /**
     * RoleHierarchy 테이블 접근 Repository
     */
    private final RoleHierarchyRepository roleHierarchyRepository;

    /**
     * ApplicationRunner
     *
     * Spring Boot 애플리케이션이 완전히 시작된 후
     * 자동 실행되는 초기화 로직이다.
     *
     * 즉
     *
     * 서버 시작 → DB Role 초기 데이터 생성
     */
    @Bean
    public ApplicationRunner initRoleData() {

        return args -> {

            /**
             * ============================
             * ROLE_USER 생성
             * ============================
             *
             * 이미 존재하면 생성하지 않는다.
             */
            roleRepository.findByRoleName("ROLE_USER")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .roleName("ROLE_USER")
                                            .description("Default user role")
                                            .build()
                            )
                    );

            /**
             * ============================
             * ROLE_ADMIN 생성
             * ============================
             */
            roleRepository.findByRoleName("ROLE_ADMIN")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .roleName("ROLE_ADMIN")
                                            .description("Administrator role")
                                            .build()
                            )
                    );

            /**
             * ============================
             * RoleHierarchy 생성
             * ============================
             *
             * ROLE_ADMIN > ROLE_USER
             *
             * 의미
             *
             * ADMIN 권한은
             * USER 권한을 포함한다.
             *
             * 즉
             *
             * ADMIN → USER API 접근 가능
             */
            if (!roleHierarchyRepository.existsByParentRoleAndChildRole(
                    "ROLE_ADMIN",
                    "ROLE_USER"
            )) {

                roleHierarchyRepository.save(

                        RoleHierarchyEntity.builder()
                                .parentRole("ROLE_ADMIN")
                                .childRole("ROLE_USER")
                                .build()
                );
            }
        };
    }
}