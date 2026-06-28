package com.example.demo.querytrace.context;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QueryTrace 요청 안에서 실행된 SQL 1건 정보.
 */
@Getter
@Builder
public class QueryTraceSql {

    /** 실행 순번. */
    private int orderNo;

    /** MyBatis mapper id. */
    private String mapperId;

    /** SQL 명령 타입. */
    private String sqlCommandType;

    /** 파라미터가 반영된 SQL. */
    private String sql;

    /** SQL 실행 시작시간. */
    private LocalDateTime executionStartTime;

    /** SQL 실행 종료시간. */
    private LocalDateTime executionEndTime;

    /** SQL 실행시간(ms). */
    private long executionTimeMs;
}
