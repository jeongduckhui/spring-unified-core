package com.example.demo.security.auth;

import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails 기반 사용자 Principal 객체
 *
 * Spring Security 인증 시스템에서는
 * 인증된 사용자 정보를 UserDetails 인터페이스로 표현한다.
 *
 * 이 클래스는 내부 User Entity를 기반으로
 * Spring Security가 이해할 수 있는 형태로 변환하는 역할을 한다.
 *
 * 현재 프로젝트에서는 OAuth2 / OIDC 로그인 구조를 사용하지만
 * 다음과 같은 상황에서 UserDetails 기반 Principal이 필요할 수 있다.
 *
 * 예
 *
 * JWT 인증
 * UsernamePasswordAuthenticationToken 생성
 * SecurityContext 저장
 * @AuthenticationPrincipal 사용
 *
 * 주요 역할
 *
 * 내부 User Entity → Spring Security UserDetails 변환
 * 사용자 권한 GrantedAuthority 생성
 * 인증 객체 Principal 역할 수행
 */
@Getter
public class CustomUserPrincipal implements UserDetails {

    /**
     * 내부 사용자 ID
     *
     * users 테이블 Primary Key
     *
     * JWT subject(sub) 값으로 사용될 수 있다.
     */
    private final Long userId;

    /**
     * 사용자 이메일
     *
     * Spring Security에서는 username 역할로 사용된다.
     */
    private final String email;

    /**
     * 사용자 권한 목록
     *
     * Spring Security는 권한 검사를
     * GrantedAuthority 기반으로 수행한다.
     *
     * 예
     *
     * ROLE_USER
     * ROLE_ADMIN
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * CustomUserPrincipal 생성자
     *
     * 내부 User Entity를 기반으로
     * Spring Security Principal 객체를 생성한다.
     */
    public CustomUserPrincipal(User user) {

        /**
         * 내부 사용자 ID 설정
         */
        this.userId = user.getId();

        /**
         * 사용자 이메일 설정
         */
        this.email = user.getEmail();

        /**
         * 사용자 권한 목록 생성
         *
         * User → UserRole → Role → roleName
         *
         * 구조를 통해 권한을 가져온다.
         */
        this.authorities =
                user.getUserRoles().stream()

                        /**
                         * UserRole → Role 변환
                         */
                        .map(UserRole::getRole)

                        /**
                         * Role → GrantedAuthority 변환
                         */
                        .map(role -> (GrantedAuthority) () -> role.getRoleName())

                        /**
                         * List로 변환
                         */
                        .collect(Collectors.toList());
    }

    /**
     * 사용자 권한 반환
     *
     * Spring Security 권한 검사 시 사용된다.
     *
     * 예
     *
     * @PreAuthorize("hasRole('ADMIN')")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 사용자 비밀번호 반환
     *
     * 현재 프로젝트는 OAuth2 / OIDC 로그인 구조이므로
     * 비밀번호를 사용하지 않는다.
     *
     * 따라서 null 반환
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * 사용자 username 반환
     *
     * Spring Security에서 username은
     * 사용자 식별자 역할을 한다.
     *
     * 여기서는 email을 사용한다.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 계정 만료 여부
     *
     * true
     *  → 계정 만료되지 않음
     *
     * false
     *  → 계정 만료됨
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     *
     * true
     *  → 계정 잠금 없음
     *
     * false
     *  → 계정 잠김
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 인증 정보 만료 여부
     *
     * true
     *  → 인증 정보 유효
     *
     * false
     *  → 인증 정보 만료
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 사용자 활성 여부
     *
     * true
     *  → 계정 활성
     *
     * false
     *  → 계정 비활성
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}