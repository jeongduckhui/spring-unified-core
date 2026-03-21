package com.example.demo.auth.service;

import com.example.demo.auth.dto.DeviceSessionResponse;
import com.example.demo.auth.exception.RefreshTokenExpiredException;
import com.example.demo.auth.exception.RefreshTokenInvalidException;
import com.example.demo.auth.exception.RefreshTokenReuseDetectedException;
import com.example.demo.security.jwt.JwtProvider;
import com.example.demo.token.domain.RefreshToken;
import com.example.demo.token.util.RefreshTokenHashUtil;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserRole;
import com.example.demo.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 인증 관련 핵심 비즈니스 로직 서비스
 *
 * 이 서비스는 다음 인증 기능을 담당한다.
 *
 * AccessToken 생성
 * RefreshToken 생성
 * RefreshToken Rotation
 * RefreshToken Reuse Attack Detection
 * 로그아웃 처리
 * 디바이스 세션 조회
 * 특정 디바이스 로그아웃
 *
 * 현재 시스템 인증 구조
 *
 * OAuth2 / OIDC Login
 *        ↓
 * 내부 JWT AccessToken 발급
 * RefreshToken 발급
 *        ↓
 * 이후 요청은 JWT 인증
 *
 * RefreshToken은 서버 DB 또는 Redis에 저장된다.
 *
 * AccessToken은 서버에 저장되지 않는 Stateless 토큰이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    /**
     * JWT 생성 및 검증 담당 Provider
     *
     * AccessToken 생성
     * JWT 검증
     * JWT 만료 시간 계산
     */
    private final JwtProvider jwtProvider;

    /**
     * RefreshToken 저장 및 관리 서비스
     *
     * DB 또는 Redis 구현체가 존재할 수 있다.
     */
    private final RefreshTokenService refreshTokenService;

    /**
     * RefreshToken 해시 유틸
     *
     * RefreshToken을 평문으로 저장하지 않기 위해
     * 해시 처리에 사용된다.
     */
    private final RefreshTokenHashUtil refreshTokenHashUtil;

    /**
     * AccessToken 블랙리스트 서비스
     *
     * 로그아웃 시 AccessToken을 무효화하기 위해 사용된다.
     */
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    /**
     * 사용자 조회 Repository
     *
     * 권한 정보 조회 시 사용된다.
     */
    private final UserRepository userRepository;

    /**
     * RefreshToken 만료 기간
     *
     * 14일
     */
    @Value("${app.auth.refresh-expire-seconds}")
    private long refreshExpireSeconds;


    /**
     * AccessToken 생성
     *
     * 로그인 성공 시 또는
     * RefreshToken으로 AccessToken 재발급 시 사용된다.
     *
     * JWT payload에는 다음 정보가 포함된다.
     *
     * userId
     * authorities
     */
    public String createAccessToken(Long userId,
                                    Collection<? extends GrantedAuthority> authorities) {

        return jwtProvider.createAccessToken(userId, authorities);
    }


    /**
     * RefreshToken 생성
     *
     * 로그인 성공 시 최초 RefreshToken 생성
     */
    public String createRefreshToken(Long userId,
                                     HttpServletRequest request,
                                     String deviceId) {

        /**
         * 랜덤 UUID를 사용하여 RefreshToken 생성
         */
        String refreshToken = UUID.randomUUID().toString();

        /**
         * RefreshToken 만료 시간 계산
         */
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(refreshExpireSeconds);

        /**
         * RefreshToken 저장
         *
         * 서버에는 해시 형태로 저장된다.
         */
        refreshTokenService.createAndSave(
                userId,
                refreshToken,
                expiresAt,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                deviceId,
                null
        );

        return refreshToken;
    }


    /**
     * RefreshToken Rotation 수행
     *
     * 기존 RefreshToken 사용 시
     * 새로운 RefreshToken을 발급하고
     * 기존 토큰을 폐기하는 방식
     */
    public RefreshResult rotateRefreshToken(String oldRefreshToken,
                                            HttpServletRequest request,
                                            String deviceId) {

        /**
         * 기존 RefreshToken 조회
         */
        RefreshToken token = refreshTokenService.findToken(oldRefreshToken)
                .orElseThrow(RefreshTokenInvalidException::new);

        /**
         * RefreshToken이 이미 revoked 상태인 경우
         *
         * 이는 Reuse Attack 가능성이 높다.
         */
        if (token.isRevoked()) {

            /**
             * 해당 사용자 모든 토큰 revoke
             *
             * 공격 가능성이 있으므로 전체 세션 종료
             */
            refreshTokenService.revokeAllByUserId(token.getUserId());

            throw new RefreshTokenReuseDetectedException();
        }

        /**
         * RefreshToken 만료 검사
         */
        if (token.isExpired()) {
            throw new RefreshTokenExpiredException();
        }

        Long userId = token.getUserId();

        /**
         * 새로운 RefreshToken 생성
         */
        String newRefreshToken = UUID.randomUUID().toString();

        /**
         * 새로운 만료 시간 설정
         */
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(refreshExpireSeconds);

        /**
         * RefreshToken Rotation 수행
         *
         * 기존 토큰 revoke
         * 새 토큰 저장
         */
        refreshTokenService.rotate(
                oldRefreshToken,
                newRefreshToken,
                expiresAt,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                deviceId
        );

        /**
         * 사용자 권한 정보 로드
         */
        Collection<? extends GrantedAuthority> authorities = loadAuthorities(userId);

        /**
         * Refresh 결과 반환
         */
        return new RefreshResult(
                userId,
                authorities,
                newRefreshToken
        );
    }


    /**
     * 로그아웃 처리
     *
     * RefreshToken revoke
     * AccessToken blacklist 등록
     */
    public void logout(String refreshToken, String accessToken) {

        /**
         * RefreshToken revoke
         */
        refreshTokenService.revoke(refreshToken);

        /**
         * AccessToken이 존재하고 유효한 경우
         * 블랙리스트에 등록
         */
        if (accessToken != null
                && !accessToken.isBlank()
                && jwtProvider.validateToken(accessToken)) {

            /**
             * AccessToken 남은 TTL 계산
             */
            long ttlMillis = jwtProvider.getRemainingExpirationMillis(accessToken);

            /**
             * 블랙리스트 등록
             */
            accessTokenBlacklistService.blacklist(accessToken, ttlMillis);
        }
    }


    /**
     * 현재 로그인된 디바이스 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DeviceSessionResponse> getActiveDevices(Long userId, String currentRefreshToken) {

        return refreshTokenService.getActiveDevices(userId, currentRefreshToken);
    }


    /**
     * 특정 디바이스 로그아웃
     */
    public void logoutDevice(Long userId, String deviceId) {

        /**
         * 해당 디바이스 RefreshToken revoke
         */
        refreshTokenService.revokeByUserIdAndDeviceId(userId, deviceId);
    }


    /**
     * 사용자 권한 정보 조회
     *
     * JWT AccessToken 생성 시 사용된다.
     */
    private Collection<? extends GrantedAuthority> loadAuthorities(Long userId) {

        /**
         * 사용자 조회
         */
        User user = userRepository.findById(userId)
                .orElseThrow();

        /**
         * UserRole → Role → 권한 문자열 변환
         */
        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}