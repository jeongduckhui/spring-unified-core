package com.example.demo.auth.controller;

import com.example.demo.auth.cookie.RefreshTokenCookieProvider;
import com.example.demo.auth.dto.DeviceSessionResponse;
import com.example.demo.auth.dto.UserMeResponse;
import com.example.demo.auth.exception.RefreshTokenNotFoundException;
import com.example.demo.auth.service.AuthService;
import com.example.demo.auth.service.RefreshResult;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.common.logging.SecurityLogger;
import com.example.demo.common.swagger.ApiCommonResponses;
import com.example.demo.security.jwt.JwtProvider;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 인증(Authentication) 관련 API Controller
 *
 * 이 컨트롤러는 다음과 같은 인증 관련 기능을 담당한다.
 *
 * refresh token rotation 처리
 * 로그아웃 처리
 * 로그인 사용자 정보 조회
 * 로그인된 디바이스 목록 조회
 * 특정 디바이스 세션 로그아웃
 *
 * 현재 프로젝트의 인증 구조
 *
 * OAuth2 / OIDC Login
 *        ↓
 * AccessToken 발급 (JWT)
 * RefreshToken 발급 (HttpOnly Cookie)
 *        ↓
 * 이후 API 요청은 JWT 기반 인증
 *
 * RefreshToken은 다음과 같은 역할을 한다.
 *
 * AccessToken 만료 시 새로운 AccessToken 발급
 * Device 기반 세션 관리
 * RefreshToken Rotation
 * RefreshToken Reuse Attack Detection
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 인증 관련 비즈니스 로직을 처리하는 서비스
     *
     * RefreshToken rotation
     * logout 처리
     * device session 조회
     * device session revoke
     */
    private final AuthService authService;

    /**
     * JWT 생성 및 검증을 담당하는 Provider
     *
     * AccessToken 생성
     * JWT parsing
     */
    private final JwtProvider jwtProvider;

    /**
     * 사용자 정보 조회용 Repository
     *
     * /auth/me API에서 사용자 정보 조회 시 사용
     */
    private final UserRepository userRepository;

    /**
     * Refresh Token provider
     * Refresh Token  생성
     * Refresh Token  삭제
     */
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    /**
     * RefreshToken 쿠키 만료 시간
     */
    @Value("${app.auth.refresh-expire-seconds}")
    private long refreshExpireSeconds;


    /**
     * AccessToken 재발급 API
     *
     * URL
     * POST /auth/refresh
     *
     * 동작 흐름
     *
     * 1. 쿠키에서 refreshToken 추출
     * 2. refreshToken 유효성 검증
     * 3. RefreshToken Rotation 수행
     * 4. 새로운 AccessToken 발급
     * 5. 새로운 RefreshToken 쿠키 설정
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        // 요청 쿠키에서 refreshToken 추출
        String refreshToken = extractRefreshTokenOrNull(request);

        // refreshToken이 없으면 인증 실패 처리
        if (refreshToken == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "unauthorized"));
        }

        // 현재 디바이스 식별자 추출
        // 프론트에서 header로 전달
        String deviceId = request.getHeader("X-Device-Id");

        // deviceId가 없는 경우 fallback 값 설정
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = "unknown";
        }

        /**
         * RefreshToken Rotation 수행
         *
         * 기존 refreshToken 폐기
         * 새로운 refreshToken 생성
         * Reuse 공격 여부 검사
         */
        RefreshResult result = authService.rotateRefreshToken(
                refreshToken,
                request,
                deviceId
        );

        /**
         * 새로운 AccessToken 생성
         *
         * JWT payload에 포함되는 정보
         *
         * userId
         * authorities
         */
        String newAccessToken = jwtProvider.createAccessToken(
                result.userId(),
                result.authorities()
        );

        /**
         * 새로운 RefreshToken 쿠키 생성
         *
         * HttpOnly
         * JavaScript 접근 차단
         *
         * XSS 공격 방지
         */
        /*
        ResponseCookie cookie = refreshTokenCookieProvider.createResponseCookie(
                result.newRefreshToken(),
                refreshExpireSeconds
        );

        response.addHeader("Set-Cookie", cookie.toString());
        */

        Cookie cookie = refreshTokenCookieProvider.createCookie(
                result.newRefreshToken(),
                refreshExpireSeconds
        );

        response.addCookie(cookie);

        // 프론트에 AccessToken 반환
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }


    /**
     * 로그아웃 API
     *
     * URL
     * POST /auth/logout
     *
     * 동작
     *
     * RefreshToken revoke
     * AccessToken blacklist 처리 가능
     * RefreshToken 쿠키 삭제
     */
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {

            String user = authentication.getName();

            SecurityLogger.logout(user, request);
        }

        // refreshToken 추출 (없으면 예외 발생)
        String refreshToken = extractRefreshToken(request);

        // accessToken 추출 (없을 수도 있음)
        String accessToken = extractAccessTokenOrNull(request);

        // logout 비즈니스 처리
        authService.logout(refreshToken, accessToken);

        /**
         * RefreshToken 쿠키 삭제
         */
        /*
        ResponseCookie cookie = refreshTokenCookieProvider.deleteResponseCookie();
        response.addHeader("Set-Cookie", cookie.toString());
        */
        Cookie cookie = refreshTokenCookieProvider.deleteCookie();
        response.addCookie(cookie);
    }


    /**
     * 현재 로그인 사용자 정보 조회 API
     *
     * URL
     * GET /auth/me
     *
     * JWT 기반 인증
     */
    @GetMapping("/me")
    public UserMeResponse me(Authentication authentication) {

        // Authentication에서 userId 추출
        Long userId = extractUserId(authentication);

        // DB에서 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow();

        // 사용자 정보를 DTO로 반환
        return UserMeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .provider(user.getProvider())
                .build();
    }


    /**
     * 로그인된 디바이스 목록 조회 API
     *
     * URL
     * GET /auth/devices
     *
     * 반환 정보
     *
     * deviceId
     * 로그인 시간
     * ip
     * userAgent
     */
    @Operation(summary = "사용자 목록 조회")
    @ApiCommonResponses
    @GetMapping("/devices")
    public List<DeviceSessionResponse> getDevices(
            Authentication authentication,
            HttpServletRequest request
    ) {

        // JWT에서 userId 추출
        Long userId = extractUserId(authentication);

        // 현재 디바이스 refreshToken
        String currentRefreshToken = extractRefreshTokenOrNull(request);

        // 활성 디바이스 목록 조회
        List<DeviceSessionResponse> activeDevices = authService.getActiveDevices(userId, currentRefreshToken);

        return activeDevices;
    }

    /*
    // 페이징 처리 예시
    @Operation(summary = "사용자 디바이스 목록 조회")
    @ApiCommonResponses
    @GetMapping("/devices")
    public PageResponse<DeviceSessionResponse> getDevices(
            Authentication authentication,
            HttpServletRequest request
    ) {

        // JWT에서 userId 추출
        Long userId = extractUserId(authentication);

        // 현재 디바이스 refreshToken
        String currentRefreshToken = extractRefreshTokenOrNull(request);

        // 활성 디바이스 목록 조회
        List<DeviceSessionResponse> activeDevices =
                authService.getActiveDevices(userId, currentRefreshToken);

        // 페이징 없이 단순 리스트일 경우
        return PageResponse.<DeviceSessionResponse>builder()
                .content(activeDevices)
                .page(0)
                .size(activeDevices.size())
                .totalElements(activeDevices.size())
                .totalPages(1)
                .last(true)
                .build();
    }
    */

    /**
     * 특정 디바이스 로그아웃
     *
     * URL
     * DELETE /auth/devices/{deviceId}
     */
    @DeleteMapping("/devices/{deviceId}")
    public Map<String, String> logoutDevice(
            @PathVariable String deviceId,
            Authentication authentication
    ) {

        // 현재 사용자 ID 추출
        Long userId = extractUserId(authentication);

        // 해당 디바이스 refreshToken revoke
        authService.logoutDevice(userId, deviceId);

        return Map.of("message", "Device session revoked");
    }


    /**
     * Authentication 객체에서 userId 추출
     *
     * Authentication principal 타입이
     * Long 또는 CustomOidcUser일 수 있기 때문에
     * 타입별 처리 수행
     */
    private Long extractUserId(Authentication authentication) {

        // 인증 정보가 없는 경우 예외
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ExceptionCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // JWT 인증 필터에서 설정한 경우
        if (principal instanceof Long userId) {
            return userId;
        }

        // OAuth2 로그인 직후
        if (principal instanceof com.example.demo.oauth2.CustomOidcUser oidcUser) {
            return oidcUser.getUserId();
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }


    /**
     * 쿠키에서 refreshToken 추출
     *
     * refreshToken이 없으면 예외 발생
     */
    private String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new RefreshTokenNotFoundException();
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new RefreshTokenNotFoundException();
    }


    /**
     * 쿠키에서 refreshToken 추출
     *
     * refreshToken이 없으면 null 반환
     */
    private String extractRefreshTokenOrNull(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }


    /**
     * Authorization Header에서 AccessToken 추출
     *
     * 형식
     *
     * Authorization: Bearer xxxx
     */
    private String extractAccessTokenOrNull(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }
}