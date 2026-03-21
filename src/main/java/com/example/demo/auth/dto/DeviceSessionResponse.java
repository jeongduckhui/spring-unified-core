package com.example.demo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 디바이스 세션 조회 API 응답 DTO
 *
 * 이 DTO는 다음 API에서 사용된다.
 *
 * GET /auth/devices
 *
 * 사용 목적
 *
 * 현재 로그인되어 있는 모든 디바이스 세션 정보를
 * 프론트엔드에 전달하기 위한 응답 객체
 *
 * 이 정보는 다음과 같은 기능에 활용된다.
 *
 * 사용자에게 현재 로그인된 디바이스 목록을 보여주기
 * 특정 디바이스 세션 로그아웃
 * 현재 사용 중인 디바이스 표시
 *
 * 예시 응답 구조
 *
 * [
 *   {
 *     "deviceId": "device-123",
 *     "userAgent": "Chrome Windows",
 *     "ipAddress": "192.168.0.10",
 *     "current": true,
 *     "createdAt": "2026-03-15T10:20:30",
 *     "expiresAt": "2026-03-29T10:20:30"
 *   }
 * ]
 *
 * 이 DTO는 Controller에서 직접 생성하지 않고
 * AuthService에서 생성하여 반환되는 경우가 일반적이다.
 */
@Getter
@Builder
@Schema(description = "디바이스 세션 조회 응답")
public class DeviceSessionResponse {

    /**
     * 디바이스 식별자
     *
     * 하나의 사용자 계정에서 여러 디바이스 로그인을 허용하기 때문에
     * 각 로그인 세션을 식별하기 위한 고유 ID가 필요하다.
     *
     * 예
     *
     * 브라우저 UUID
     * 모바일 앱 설치 UUID
     *
     * 이 값은 RefreshToken과 연결되어
     * 특정 디바이스 세션을 종료할 때 사용된다.
     */
    @Schema(description = "디바이스 식별자", example = "\tff005ecc-fc8c-422e-b1a7-e57b498061e2")
    private String deviceId;

    /**
     * 사용자 브라우저 정보
     *
     * HTTP Request Header의
     * User-Agent 값을 저장한다.
     *
     * 예
     *
     * Mozilla/5.0 (Windows NT 10.0; Win64; x64)
     *
     * 프론트 화면에서는
     *
     * Chrome / Windows
     * Safari / iPhone
     *
     * 형태로 가공하여 표시할 수 있다.
     */
    @Schema(description = "사용자 브라우저 정보", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    /**
     * 로그인 요청 IP 주소
     *
     * 보안 목적 및 세션 관리 목적
     *
     * 활용 예
     *
     * 의심스러운 로그인 탐지
     * 로그인 기록 조회
     * 보안 감사 로그
     */
    @Schema(description = "로그인 요청 IP 주소", example = "127.0.0.1")
    private String ipAddress;

    /**
     * 현재 사용 중인 디바이스 여부
     *
     * 사용자가 지금 보고 있는 브라우저인지 여부를 표시한다.
     *
     * 예
     *
     * true
     * 현재 로그인된 브라우저
     *
     * false
     * 다른 디바이스 로그인
     *
     * 프론트에서는
     *
     * "현재 사용 중"
     *
     * 같은 UI 표시를 할 수 있다.
     */
    @Schema(description = "현재 사용 중인 디바이스 여부", example = "false")
    private boolean current;

    /**
     * RefreshToken 생성 시간
     *
     * 즉, 이 디바이스가 로그인된 시간이다.
     *
     * 사용 예
     *
     * "2026-03-15 10:30 로그인"
     */
    @Schema(description = "RefreshToken 생성 시간", example = "2026-03-15 10:30")
    private LocalDateTime createdAt;

    /**
     * RefreshToken 만료 시간
     *
     * 현재 디바이스 세션이 언제 만료되는지 표시한다.
     *
     * RefreshToken이 만료되면
     *
     * AccessToken 재발급이 불가능해지고
     * 다시 로그인해야 한다.
     */
    @Schema(description = "RefreshToken 만료 시간", example = "2026-03-15 10:30")
    private LocalDateTime expiresAt;
}