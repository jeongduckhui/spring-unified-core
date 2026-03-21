package com.example.demo.user.repository;

import com.example.demo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Repository
 *
 * 사용자(User) 엔티티를 관리하는 JPA Repository이다.
 *
 * 주요 역할
 *
 * - 사용자 조회
 * - 사용자 저장
 * - OAuth2/OIDC 로그인 사용자 조회
 *
 * JpaRepository를 상속하기 때문에
 * 기본 CRUD 기능을 자동으로 제공한다.
 *
 * 제공되는 기본 메서드 예
 *
 * save()
 * findById()
 * findAll()
 * delete()
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     *
     * 사용 목적
     *
     * OAuth2 / OIDC 로그인 시
     * 기존 사용자 계정과 매칭하기 위해 사용한다.
     *
     * 예
     *
     * Google 로그인
     * email = test@gmail.com
     *
     * DB에 동일 이메일이 존재하면
     * 기존 사용자로 처리한다.
     *
     * 생성되는 SQL 예
     *
     * SELECT *
     * FROM users
     * WHERE email = ?
     */
    Optional<User> findByEmail(String email);


    /**
     * OAuth2 Provider + Provider Subject(sub)으로 사용자 조회
     *
     * OAuth2 / OIDC 로그인 시
     * 가장 정확한 사용자 식별 방법이다.
     *
     * 예
     *
     * provider = GOOGLE
     * provider_subject = 109234982348923489
     *
     * provider_subject는
     * OAuth2/OIDC의 고유 사용자 ID(sub)이다.
     *
     * 생성되는 SQL 예
     *
     * SELECT *
     * FROM users
     * WHERE provider = ?
     *   AND provider_subject = ?
     *
     * 반환값
     *
     * Optional<User>
     *
     * 이유
     *
     * 해당 사용자가 DB에 없을 수 있기 때문이다.
     */
    Optional<User> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );

}