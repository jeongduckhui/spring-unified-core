package com.example.demo.user.domain;

/**
 * 사용자 계정 상태 Enum
 *
 * User 엔티티의 status 필드에서 사용된다.
 *
 * 사용자 계정의 현재 상태를 나타낸다.
 *
 * 예
 *
 * ACTIVE     → 정상 계정
 * LOCKED     → 잠긴 계정
 * WITHDRAWN  → 탈퇴 계정
 *
 * 이 상태값은 다음과 같은 상황에서 사용된다.
 *
 * 로그인 가능 여부 판단
 * 관리자 계정 제어
 * 탈퇴 사용자 관리
 *
 * User 엔티티에서는 다음 방식으로 저장된다.
 *
 * @Enumerated(EnumType.STRING)
 *
 * 즉 DB에는 Enum 이름이 문자열로 저장된다.
 *
 * 예
 *
 * ACTIVE
 * LOCKED
 * WITHDRAWN
 */
public enum UserStatus {

    /**
     * 정상 사용자
     *
     * 로그인 가능
     * API 접근 가능
     */
    ACTIVE,

    /**
     * 잠긴 사용자
     *
     * 예
     *
     * 관리자 계정 잠금
     * 보안 정책 위반
     * 로그인 시도 실패 제한
     *
     * 로그인 차단 상태
     */
    LOCKED,

    /**
     * 탈퇴 사용자
     *
     * 사용자 탈퇴 처리 시 사용
     *
     * 보통 실제 삭제 대신
     * Soft Delete 방식으로 관리한다.
     *
     * 즉
     *
     * DB 데이터는 유지
     * 로그인은 불가
     */
    WITHDRAWN
}