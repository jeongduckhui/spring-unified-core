package com.example.demo.oauth2;

import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserRole;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.repository.RoleRepository;
import com.example.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

/**
 * OIDC(OpenID Connect) 로그인 사용자 처리 서비스
 *
 * Spring Security OIDC 로그인 과정에서
 * Provider로부터 받은 사용자 정보를 처리하는 핵심 서비스이다.
 *
 * 주요 역할
 *
 * OIDC Provider 사용자 정보 조회
 * 내부 시스템 사용자 조회
 * 필요 시 신규 사용자 생성
 * 사용자 권한 로딩
 * CustomOidcUser 생성
 *
 * 전체 로그인 흐름
 *
 * 사용자 로그인
 * ↓
 * OIDC Provider 인증
 * ↓
 * Spring Security OidcAuthorizationCodeAuthenticationProvider
 * ↓
 * CustomOidcUserService.loadUser()
 * ↓
 * 내부 사용자 조회 또는 생성
 * ↓
 * CustomOidcUser 반환
 * ↓
 * SecurityContext 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOidcUserService extends OidcUserService {

    /**
     * 신규 사용자에게 기본으로 부여할 역할
     */
    private static final String DEFAULT_ROLE = "ROLE_USER";

    /**
     * 사용자 Repository
     *
     * users 테이블 접근
     */
    private final UserRepository userRepository;

    /**
     * 역할 Repository
     *
     * roles 테이블 접근
     */
    private final RoleRepository roleRepository;

    /**
     * OIDC 로그인 시 호출되는 핵심 메서드
     *
     * OIDC Provider로부터 사용자 정보를 받아 처리한다.
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        /**
         * 부모 클래스의 loadUser 호출
         *
         * 실제 OIDC Provider API를 호출하여
         * 사용자 정보를 가져온다.
         */
        OidcUser oidcUser = super.loadUser(userRequest);

        /**
         * OAuth2 / OIDC Provider 식별자
         *
         * 예
         *
         * google
         * keycloak
         * azure
         */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        /**
         * Provider가 반환한 사용자 attributes
         *
         * 예 (OIDC ID Token)
         *
         * {
         *   "sub": "...",
         *   "email": "...",
         *   "name": "...",
         *   "preferred_username": "..."
         * }
         */
        Map<String, Object> attributes = oidcUser.getAttributes();

        /**
         * Provider별 사용자 attribute 구조를
         * 공통 모델로 변환한다.
         */
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(registrationId, attributes);

        /**
         * 내부 사용자 조회 또는 생성
         */
        User user = findOrCreateUser(oAuth2Attributes);

        /**
         * 사용자 권한 목록 생성
         *
         * UserRole → Role → roleName → GrantedAuthority 변환
         */
        Collection<? extends GrantedAuthority> authorities =
                user.getUserRoles().stream()
                        .map(UserRole::getRole)
                        .map(Role::getRoleName)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        /**
         * CustomOidcUser 생성
         *
         * Spring Security SecurityContext에 저장된다.
         */
        return new CustomOidcUser(
                user.getId(),
                user.getEmail(),
                user.getProvider(),
                user.getProviderSubject(),
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }

    /**
     * 내부 사용자 조회 또는 생성
     *
     * 조회 순서
     *
     * 1 provider + providerSubject 조회
     * 2 email 조회
     * 3 신규 사용자 생성
     */
    private User findOrCreateUser(OAuth2Attributes attributes) {

        return userRepository.findByProviderAndProviderSubject(
                        attributes.provider(),
                        attributes.providerSubject()
                )

                /**
                 * provider 기반 사용자 존재 시
                 * 프로필 업데이트
                 */
                .map(existingUser -> updateExistingUser(existingUser, attributes))

                /**
                 * provider 기반 계정이 없으면
                 * email 기준으로 사용자 조회
                 */
                .or(() -> userRepository.findByEmail(attributes.email())
                        .map(existingUser -> updateExistingUser(existingUser, attributes)))

                /**
                 * 사용자 없으면 신규 생성
                 */
                .orElseGet(() -> createNewUser(attributes));
    }

    /**
     * 기존 사용자 업데이트
     */
    private User updateExistingUser(User user, OAuth2Attributes attributes) {

        /**
         * 이메일 / 이름 업데이트
         */
        user.updateProfile(attributes.email(), attributes.name());

        /**
         * 기본 역할 추가
         */
        user.addRole(getDefaultRole());

        return user;
    }

    /**
     * 신규 사용자 생성
     */
    private User createNewUser(OAuth2Attributes attributes) {

        /**
         * User Entity 생성
         */
        User newUser = User.builder()
                .provider(attributes.provider())
                .providerSubject(attributes.providerSubject())
                .email(attributes.email())
                .name(attributes.name())
                .status(UserStatus.ACTIVE)
                .build();

        /**
         * 기본 사용자 역할 부여
         */
        newUser.addRole(getDefaultRole());

        /**
         * DB 저장
         */
        return userRepository.save(newUser);
    }

    /**
     * 기본 사용자 역할 조회
     *
     * roles 테이블에서 ROLE_USER 조회
     */
    private Role getDefaultRole() {

        return roleRepository.findByRoleName(DEFAULT_ROLE)

                /**
                 * ROLE_USER 존재하지 않으면
                 * 시스템 설정 오류
                 */
                .orElseThrow(() ->
                        new IllegalStateException("Default role not found: " + DEFAULT_ROLE));
    }
}