package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduling 기능 활성화 설정 클래스
 *
 * Spring Boot에서
 * @Scheduled 어노테이션 기반의 스케줄링 기능을 사용하려면
 * 반드시 @EnableScheduling 설정이 필요하다.
 *
 * 이 설정이 활성화되면
 * Spring Container가 애플리케이션 실행 시
 * @Scheduled가 붙은 메서드를 자동으로 탐지하여
 * 스케줄링 작업을 수행한다.
 *
 * --------------------------------------------------
 * 동작 방식
 * --------------------------------------------------
 *
 * 1. Spring Boot Application 시작
 *
 * 2. @EnableScheduling 감지
 *
 * 3. Spring이 내부적으로 Scheduler 생성
 *
 * 4. @Scheduled 메서드 스캔
 *
 * 5. 지정된 주기에 따라 자동 실행
 *
 * --------------------------------------------------
 * 예시
 * --------------------------------------------------
 *
 * @Scheduled(fixedRate = 60000)
 * public void cleanupToken() {
 *     // 1분마다 실행
 * }
 *
 * 또는
 *
 * @Scheduled(cron = "0 0 3 * * *")
 * public void dailyJob() {
 *     // 매일 새벽 3시 실행
 * }
 *
 * --------------------------------------------------
 * 현재 SSO 프로젝트에서 예상되는 사용 예
 * --------------------------------------------------
 *
 * 1. 만료된 RefreshToken 정리
 *
 * 2. Redis Token Cleanup
 *
 * 3. Device Session 정리
 *
 * 4. 보안 로그 정리
 *
 * 예:
 *
 * RefreshTokenScheduler
 *
 * @Scheduled(cron = "0 0 * * * *")
 * → 1시간마다 만료 토큰 정리
 *
 * --------------------------------------------------
 * 실무 팁
 * --------------------------------------------------
 *
 * Scheduling은 단일 서버에서는 문제 없지만
 * 멀티 서버 환경에서는 중복 실행 문제가 발생할 수 있다.
 *
 * 예:
 *
 * 서버 3대
 * → 스케줄러 3번 실행
 *
 * 해결 방법
 *
 * 1. Redis Lock
 * 2. ShedLock 라이브러리
 * 3. DB Lock
 *
 * 현재 프로젝트는
 * 테스트/POC 성격이므로 기본 Scheduling만 사용해도 충분하다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * 이 클래스는 별도의 Bean을 정의하지 않는다.
     *
     * 역할은 단 하나
     *
     * → Spring Scheduling 기능 활성화
     *
     * 실제 스케줄 작업은
     * 아래와 같은 클래스에서 정의된다.
     *
     * 예:
     *
     * RefreshTokenCleanupScheduler
     *
     * @Component
     * public class RefreshTokenCleanupScheduler {
     *
     *     @Scheduled(cron = "0 0 * * * *")
     *     public void cleanup() {
     *         // 만료된 토큰 정리
     *     }
     *
     * }
     */
}