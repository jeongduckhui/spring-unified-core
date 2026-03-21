package com.example.demo.token.cleanup;

import com.example.demo.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RefreshToken 정리 스케줄러
 *
 * RefreshToken은 로그인할 때마다 생성되기 때문에
 * 시간이 지나면 DB에 토큰 데이터가 계속 쌓이게 된다.
 *
 * 따라서 다음 두 가지 데이터를 주기적으로 정리해야 한다.
 *
 * 1 만료된 토큰
 * 2 revoke된 토큰 (일정 기간 보관 후 삭제)
 *
 * 이 클래스는 위 작업을 자동으로 수행한다.
 */
@Slf4j
@Component
@Profile("db")
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    /**
     * RefreshToken DB Repository
     *
     * 토큰 삭제 쿼리를 수행하기 위해 사용된다.
     */
    private final RefreshTokenRepository refreshTokenRepository;

    /*
     * ===== 정책 =====
     *
     * revoke된 토큰은 바로 삭제하지 않는다.
     *
     * 이유
     *
     * - refresh token reuse attack 탐지
     * - 보안 감사 로그
     *
     * 일정 기간 보관 후 삭제한다.
     *
     * 현재 정책
     *
     * revoke 후 30일 보관
     */
    private static final int REVOKED_RETENTION_DAYS = 30;

    /*
     * ===== 실행 스케줄 =====
     *
     * 매일 새벽 3시 실행
     *
     * cron 표현식 설명
     *
     * 초 분 시 일 월 요일
     *
     * 0 0 3 * * *
     *
     * → 매일 03:00:00 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupRefreshTokens() {

        /**
         * 현재 시간
         */
        LocalDateTime now = LocalDateTime.now();

        /**
         * revoke 토큰 삭제 기준 시간
         *
         * revoke 후 30일 지난 토큰 삭제
         */
        LocalDateTime revokedCutoff = now.minusDays(REVOKED_RETENTION_DAYS);

        /**
         * 만료된 refreshToken 삭제
         *
         * expiresAt < now
         */
        long expiredDeleted = refreshTokenRepository.deleteByExpiresAtBefore(now);

        /**
         * revoke된 토큰 중
         * revokedAt 기준 30일 지난 토큰 삭제
         */
        long revokedDeleted =
                refreshTokenRepository.deleteByRevokedTrueAndRevokedAtBefore(revokedCutoff);

        /**
         * 스케줄러 실행 결과 로그 출력
         */
        log.info(
                "RefreshToken cleanup completed. expiredDeleted={}, revokedDeleted={}, now={}, revokedCutoff={}",
                expiredDeleted,
                revokedDeleted,
                now,
                revokedCutoff
        );
    }
}