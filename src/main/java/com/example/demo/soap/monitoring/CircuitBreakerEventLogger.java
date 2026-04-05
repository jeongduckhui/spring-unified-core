package com.example.demo.soap.monitoring;

import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CircuitBreaker 상태 변경 로그 처리 클래스
 *
 * 역할:
 * - Resilience4j CircuitBreaker 상태 변화 감지
 * - 상태 전환 시 로그 출력
 *
 * 특징:
 * - 직접 호출되는 코드 없음
 * - Spring Event 기반으로 자동 실행
 *
 * 동작 흐름:
 * CircuitBreaker 상태 변경 발생
 *   → Resilience4j 내부에서 이벤트 발행
 *   → Spring EventListener가 해당 이벤트 수신
 *   → onStateChange() 자동 실행
 *
 * 사용 목적:
 * - 외부 시스템 장애 감지
 * - CircuitBreaker 동작 확인
 * - 운영 로그 분석
 */
@Slf4j
@Component
public class CircuitBreakerEventLogger {

    /**
     * CircuitBreaker 상태 변경 이벤트 리스너
     *
     * 실행 시점:
     * - CircuitBreaker 상태가 바뀔 때마다 자동 실행됨
     *
     * 상태 종류:
     * CLOSED
     *   정상 상태 (요청 허용)
     *
     * OPEN
     *   장애 상태 (요청 차단)
     *
     * HALF_OPEN
     *   복구 확인 상태 (일부 요청만 허용)
     *
     * 주요 전환 흐름:
     *
     * CLOSED → OPEN
     *   외부 시스템 장애 발생
     *
     * OPEN → HALF_OPEN
     *   일정 시간 후 테스트 요청 허용
     *
     * HALF_OPEN → CLOSED
     *   정상 복구
     *
     * HALF_OPEN → OPEN
     *   여전히 장애 상태
     *
     * 예시 로그:
     * CircuitBreaker state changed. name=approvalSoapCircuitBreaker, from=CLOSED, to=OPEN
     *
     * 의미:
     * 승인 SOAP 외부 시스템 장애 발생
     */
    @EventListener
    public void onStateChange(CircuitBreakerOnStateTransitionEvent event) {

        /**
         * CircuitBreaker 이름
         * 예: approvalSoapCircuitBreaker
         */
        String name = event.getCircuitBreakerName();

        /**
         * 이전 상태
         */
        String from = event.getStateTransition().getFromState().name();

        /**
         * 변경된 상태
         */
        String to = event.getStateTransition().getToState().name();

        /**
         * 상태 변경 로그 출력
         *
         * 운영에서 매우 중요한 로그
         * - 장애 발생 시점 추적
         * - 복구 시점 확인
         */
        log.warn(
                "CircuitBreaker state changed. name={}, from={}, to={}",
                name,
                from,
                to
        );
    }
}