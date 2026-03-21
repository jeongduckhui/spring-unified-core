package com.example.demo.auth.service;

/**
 * AccessToken 블랙리스트 관리 서비스 인터페이스
 *
 * JWT 기반 인증 시스템에서는 일반적으로 AccessToken을 서버에 저장하지 않는다.
 *
 * 즉, AccessToken은 Stateless 구조로 동작한다.
 *
 * 문제
 *
 * 사용자가 로그아웃해도 이미 발급된 AccessToken은
 * 만료될 때까지 계속 사용할 수 있다.
 *
 * 예
 *
 * AccessToken 만료 시간
 * 30분
 *
 * 사용자가 로그아웃
 *
 * 공격자가 이미 탈취한 AccessToken을
 * 만료 전까지 계속 사용할 수 있음
 *
 * 해결 방법
 *
 * AccessToken Blacklist
 *
 * 로그아웃 시 AccessToken을 블랙리스트에 등록하고
 * 이후 요청에서 해당 토큰이 블랙리스트에 존재하면
 * 인증을 거부한다.
 *
 * 일반적인 구현 방식
 *
 * Redis 사용
 *
 * 이유
 *
 * AccessToken 만료 시간까지만 저장하면 되기 때문에
 * TTL 기반 저장소가 적합하다.
 *
 * 동작 흐름
 *
 * 사용자 로그아웃
 * ↓
 * AccessToken을 블랙리스트 저장
 * ↓
 * 이후 요청
 * ↓
 * JwtAuthenticationFilter에서 블랙리스트 검사
 * ↓
 * 블랙리스트 존재 시 인증 거부
 */
public interface AccessTokenBlacklistService {

    /**
     * AccessToken을 블랙리스트에 등록한다.
     *
     * 사용 시점
     *
     * 로그아웃 처리 시
     *
     * 동작
     *
     * AccessToken을 Redis 등에 저장하고
     * 토큰의 남은 만료 시간(TTL) 동안 유지한다.
     *
     * TTL을 사용하는 이유
     *
     * AccessToken이 원래 만료되면
     * 블랙리스트에서도 자동 삭제되도록 하기 위함이다.
     *
     * 예
     *
     * AccessToken 남은 시간
     * 10분
     *
     * Redis 저장
     *
     * key   : blacklist:accessToken
     * value : true
     * TTL   : 10분
     *
     * @param accessToken 블랙리스트에 등록할 AccessToken
     * @param ttlMillis   AccessToken 남은 만료 시간 (밀리초)
     */
    void blacklist(String accessToken, long ttlMillis);

    /**
     * AccessToken이 블랙리스트에 등록되어 있는지 확인한다.
     *
     * 사용 위치
     *
     * JwtAuthenticationFilter
     *
     * 요청이 들어올 때마다 다음 검사를 수행한다.
     *
     * Authorization Header에서 AccessToken 추출
     * ↓
     * JWT 검증
     * ↓
     * 블랙리스트 검사
     * ↓
     * 블랙리스트 존재 시 인증 실패
     *
     * 예
     *
     * if (blacklistService.isBlacklisted(token)) {
     *     throw new InvalidTokenException();
     * }
     *
     * @param accessToken 검사할 AccessToken
     * @return true  블랙리스트에 존재
     * @return false 정상 토큰
     */
    boolean isBlacklisted(String accessToken);
}