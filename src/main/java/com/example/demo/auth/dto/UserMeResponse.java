package com.example.demo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 사용자 정보 조회 응답 DTO
 *
 * 사용 API
 *
 * GET /auth/me
 *
 * 목적
 *
 * 현재 로그인한 사용자의 기본 정보를
 * 프론트엔드에 전달하기 위한 응답 객체이다.
 *
 * 이 API는 일반적으로 다음 상황에서 사용된다.
 *
 * 프론트 애플리케이션 초기 로딩 시 사용자 정보 조회
 * 로그인 상태 확인
 * 사용자 프로필 정보 표시
 *
 * 예시 응답
 *
 * {
 *   "userId": 1,
 *   "email": "user@example.com",
 *   "provider": "google"
 * }
 *
 * 이 DTO는 Entity(User)를 직접 반환하지 않고
 * 필요한 정보만 선택하여 전달하기 위한 용도로 사용된다.
 *
 * 이유
 *
 * Entity를 그대로 반환하면
 * 불필요한 정보가 노출될 수 있기 때문이다.
 */
@Getter
@Builder
@Schema(description = "사용자 조회 응답")
public class UserMeResponse {

    /**
     * 사용자 고유 ID
     *
     * DB의 users 테이블 Primary Key 값
     *
     * 이 값은 시스템 내부에서 사용자를 식별하기 위한
     * 가장 기본적인 식별자이다.
     *
     * 현재 프로젝트의 JWT 구조에서도
     * 이 값이 JWT의 subject(sub)에 들어간다.
     *
     * 예
     *
     * JWT payload
     *
     * {
     *   "sub": 1
     * }
     */
    @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 사용자 이메일
     *
     * OAuth2 / OIDC 로그인 시
     * Identity Provider에서 전달받는 이메일 정보
     *
     * 예
     *
     * Google 로그인
     * Kakao 로그인
     * Keycloak 로그인
     *
     * 이 시스템에서는
     * 이메일을 기준으로 사용자 계정을 식별하거나
     * 계정을 연결할 수 있다.
     */
    @Schema(description = "사용자 이메일", example = "user@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 로그인 제공자 (Identity Provider)
     *
     * 사용자가 어떤 인증 제공자를 통해 로그인했는지 표시한다.
     *
     * 예
     *
     * google
     * kakao
     * keycloak
     * naver
     *
     * 이 정보는 다음 상황에서 유용하다.
     *
     * 사용자 계정 관리
     * 로그인 제공자 표시
     * 계정 연결 관리
     */
    @Schema(description = "로그인 제공자", example = "KEYCLOAK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String provider;

}