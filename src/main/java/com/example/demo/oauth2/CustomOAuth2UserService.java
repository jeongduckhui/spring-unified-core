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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 사용자 처리 서비스
 *
 * Spring Security OAuth2 로그인 흐름에서
 * Provider로부터 받은 사용자 정보를 처리하는 핵심 서비스이다.
 *
 * 이 서비스의 역할
 *
 * OAuth2 Provider 사용자 정보 조회
 * 내부 시스템 User 조회
 * 필요 시 신규 사용자 생성
 * 사용자 권한 로딩
 * CustomOAuth2User 생성
 *
 * 전체 OAuth2 로그인 흐름
 *
 * 사용자 로그인 요청
 * ↓
 * OAuth2 Provider (Google / Naver / Keycloak 등)
 * ↓
 * Spring Security OAuth2LoginAuthenticationFilter
 * ↓
 * CustomOAuth2UserService.loadUser()
 * ↓
 * 내부 사용자 조회 또는 생성
 * ↓
 * CustomOAuth2User 반환
 * ↓
 * SecurityContext 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * 기본 사용자 역할
     *
     * 신규 사용자 생성 시 자동으로 부여된다.
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
     * OAuth2 로그인 시 호출되는 핵심 메서드
     *
     * Spring Security OAuth2 인증 흐름에서
     * Provider 사용자 정보를 가져온 후 호출된다.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        /**
         * 기본 OAuth2 사용자 서비스
         *
         * Provider로부터 사용자 정보를 가져오는 역할을 한다.
         */
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        /**
         * 실제 OAuth2 Provider API 호출
         *
         * Google / Naver / Kakao / Keycloak 등에서
         * 사용자 정보를 조회한다.
         */
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        /**
         * OAuth2 Provider 식별자
         *
         * 예
         *
         * google
         * naver
         * kakao
         * keycloak
         */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        /**
         * OAuth2 Provider가 반환한 사용자 attributes
         *
         * 예
         *
         * Google
         *
         * {
         *   "sub": "...",
         *   "email": "...",
         *   "name": "...",
         *   "picture": "..."
         * }
         */
        Map<String, Object> attributes = oAuth2User.getAttributes();

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
        Collection<? extends GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .map(SimpleGrantedAuthority::new)
                .toList();

        /**
         * CustomOAuth2User 생성
         *
         * 이후 SecurityContext에 저장된다.
         */
        return new CustomOAuth2User(
                user.getId(),
                user.getEmail(),
                user.getProvider(),
                user.getProviderSubject(),
                authorities,
                attributes
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
     *
     * 이메일 / 이름 업데이트
     */
    private User updateExistingUser(User user, OAuth2Attributes attributes) {

        /**
         * 사용자 프로필 업데이트
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
         * 기본 역할 부여
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