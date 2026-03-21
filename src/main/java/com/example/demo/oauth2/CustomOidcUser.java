package com.example.demo.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Collection;
import java.util.Map;

/**
 * OIDC(OpenID Connect) 로그인 사용자 Principal 객체
 *
 * Spring Security OIDC 로그인 성공 시
 * SecurityContext에 저장되는 사용자 객체이다.
 *
 * 이 클래스는 DefaultOidcUser를 상속하여
 * 내부 시스템 사용자 정보(userId 등)를 추가로 포함한다.
 *
 * OIDC는 OAuth2의 확장 프로토콜로
 * "사용자 인증" 정보를 포함한다.
 *
 * 주요 특징
 *
 * OAuth2 → 단순 Authorization
 * OIDC   → Authentication + Identity
 *
 * OIDC 로그인 시 반환되는 주요 데이터
 *
 * id_token
 * userinfo
 *
 * 이 클래스는 해당 정보를 기반으로
 * 내부 사용자 정보를 함께 관리한다.
 *
 * 로그인 흐름
 *
 * 사용자 로그인
 * ↓
 * OIDC Provider 인증
 * ↓
 * CustomOidcUserService
 * ↓
 * CustomOidcUser 생성
 * ↓
 * SecurityContext 저장
 */
@Getter
public class CustomOidcUser extends DefaultOidcUser {

    /**
     * 내부 시스템 사용자 ID
     *
     * users 테이블 Primary Key
     *
     * JWT 생성 시 subject(sub) 값으로 사용된다.
     */
    private final Long userId;

    /**
     * 사용자 이메일
     *
     * OIDC Provider에서 전달받은 이메일 정보
     */
    private final String email;

    /**
     * OAuth2 / OIDC Provider 이름
     *
     * 예
     *
     * google
     * keycloak
     * azure
     */
    private final String provider;

    /**
     * Provider 사용자 고유 식별자
     *
     * 예
     *
     * OIDC ID Token의 sub 값
     *
     * 내부 사용자 계정 매핑 시 사용된다.
     *
     * provider + providerSubject
     *
     * 조합으로 외부 계정을 식별한다.
     */
    private final String providerSubject;

    /**
     * CustomOidcUser 생성자
     *
     * OIDC 로그인 성공 시
     * CustomOidcUserService에서 호출된다.
     */
    public CustomOidcUser(

            /**
             * 내부 사용자 ID
             */
            Long userId,

            /**
             * 사용자 이메일
             */
            String email,

            /**
             * OAuth2 / OIDC Provider 이름
             */
            String provider,

            /**
             * Provider 사용자 고유 ID
             */
            String providerSubject,

            /**
             * 사용자 권한 목록
             *
             * Spring Security에서 권한 검사에 사용된다.
             */
            Collection<? extends GrantedAuthority> authorities,

            /**
             * OIDC ID Token
             *
             * OIDC 로그인 시 발급되는 JWT
             *
             * 주요 정보
             *
             * sub
             * email
             * name
             * exp
             * iss
             */
            OidcIdToken idToken,

            /**
             * OIDC UserInfo
             *
             * 추가 사용자 정보 endpoint에서
             * 조회된 데이터이다.
             */
            OidcUserInfo userInfo
    ) {

        /**
         * DefaultOidcUser 생성자 호출
         *
         * Spring Security 내부에서
         * idToken과 userInfo를 사용하여
         * 기본 OIDC 사용자 정보를 관리한다.
         */
        super(authorities, idToken, userInfo);

        /**
         * 내부 사용자 정보 설정
         */
        this.userId = userId;
        this.email = email;
        this.provider = provider;
        this.providerSubject = providerSubject;
    }
}