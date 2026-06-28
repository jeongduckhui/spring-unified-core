package com.example.demo.querytrace.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

/**
 * QueryTrace 운영 정책 설정.
 *
 * <p>
 * 쿼리보기 기능의 Redis 보관 정책을 코드가 아니라 설정값으로 관리한다.
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "query-trace")
public class QueryTraceProperties {

    /**
     * 사용자 + 화면(Route Path) 기준 최대 QueryTrace 보관 건수.
     *
     * <p>
     * 전체 시스템 기준 20건이 아니라, 아래와 같은 단위별 보관 건수이다.
     * </p>
     *
     * <pre>
     * userA + /sample → 최근 20건
     * userA + /sales  → 최근 20건
     * userB + /sample → 최근 20건
     * </pre>
     */
    private int maxTraceCountPerScreen = 20;

    /**
     * Redis Key 만료 기준 시각.
     *
     * <p>
     * 기본값은 하루가 끝나기 직전인 23:59:59이다.
     * </p>
     */
    private LocalTime expireTime = LocalTime.of(23, 59, 59);

    /**
     * 만료시각 계산 기준 지역.
     */
    private String zoneId = "Asia/Seoul";

    /**
     * ZoneId 객체 반환.
     *
     * @return ZoneId
     */
    public ZoneId getZoneIdValue() {
        return ZoneId.of(zoneId);
    }
}
