package com.example.demo.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 사용자 정보를 표현하는 Custom Principal 객체
 *
 * Spring Security OAuth2 로그인 성공 후
 * SecurityContext에 저장되는 사용자 객체이다.
 *
 * 기본적으로 Spring Security는 OAuth2 로그인 시
 * DefaultOAuth2User 객체를 사용하지만,
 * 우리는 내부 시스템 사용자 구조에 맞게 CustomOAuth2User를 사용한다.
 *
 * 주요 역할
 *
 * OAuth2 Provider 사용자 정보 보관
 * 내부 시스템 userId 보관
 * 사용자 권한(authorities) 보관
 * OAuth2 attributes 보관
 *
 * Spring Security 흐름
 *
 * OAuth2 로그인 성공
 * ↓
 * CustomOAuth2UserService
 * ↓
 * CustomOAuth2User 생성
 * ↓
 * SecurityContext에 저장
 *
 * 이후 Controller 또는 SuccessHandler에서
 * Authentication.getPrincipal()로 접근 가능하다.
 */
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    /**
     * 내부 시스템 사용자 ID (Primary Key)
     *
     * OAuth2 로그인 후
     * 내부 users 테이블과 매핑된 ID이다.
     *
     * JWT 생성 시 sub(subject)로 사용된다.
     *
     * 예
     *
     * JWT payload
     *
     * {
     *   "sub": userId
     * }
     */
    private final Long userId;

    /**
     * 사용자 이메일
     *
     * OAuth2 Provider에서 전달받은 이메일
     *
     * Google
     * Naver
     * Kakao
     *
     * 등의 로그인 시 사용된다.
     *
     * 내부 사용자 계정 매핑 기준으로 사용될 수 있다.
     */
    private final String email;

    /**
     * OAuth2 Provider 이름
     *
     * 예
     *
     * google
     * naver
     * kakao
     * keycloak
     *
     * 내부 사용자 계정 관리 시
     * provider + providerSubject 조합으로
     * 외부 계정을 식별할 수 있다.
     */
    private final String provider;

    /**
     * OAuth2 Provider 사용자 고유 ID
     *
     * OAuth2 Provider가 제공하는 사용자 식별자이다.
     *
     * 예
     *
     * Google → sub
     * Naver → id
     * Kakao → id
     *
     * 내부 사용자 테이블에서는
     *
     * provider
     * providerSubject
     *
     * 조합으로 외부 계정을 식별한다.
     */
    private final String providerSubject;

    /**
     * 사용자 권한 목록
     *
     * Spring Security에서 사용하는 권한 객체이다.
     *
     * 예
     *
     * ROLE_USER
     * ROLE_ADMIN
     *
     * 이 권한 정보는
     *
     * AccessToken 생성 시
     * SecurityContext 권한 검사 시
     *
     * 사용된다.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * OAuth2 Provider에서 전달받은 원본 사용자 정보
     *
     * 예
     *
     * Google 로그인 attributes
     *
     * {
     *   "sub": "...",
     *   "email": "...",
     *   "name": "...",
     *   "picture": "..."
     * }
     *
     * 필요한 경우 추가 사용자 정보를 가져오기 위해 사용한다.
     */
    private final Map<String, Object> attributes;

    /**
     * OAuth2User 인터페이스 구현 메서드
     *
     * Spring Security는 OAuth2User에서
     * getName() 값을 "사용자 식별자"로 사용한다.
     *
     * 여기서는 내부 사용자 ID를 반환한다.
     *
     * 예
     *
     * Authentication.getName()
     *
     * 호출 시 이 값이 반환된다.
     */
    @Override
    public String getName() {

        /**
         * userId를 문자열로 변환하여 반환
         */
        return String.valueOf(userId);
    }
}