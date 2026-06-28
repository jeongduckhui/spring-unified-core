package com.example.demo.querytrace.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 요청 1건의 QueryTrace 상태.
 */
@Getter
@Builder
public class QueryTraceContext {

    /** trace 식별자. */
    private String traceId;

    /** 사용자 ID. */
    private String userId;

    /** 화면 ID. React route path 사용. */
    private String screenId;

    /** 요청 URI. */
    private String requestUri;

    /** Controller 클래스명. */
    private String controllerName;

    /** Controller 메서드명. */
    private String methodName;

    /** 어노테이션 설명. */
    private String description;

    /** 트랜잭션 시작시간. */
    private LocalDateTime transactionStartTime;

    /** 트랜잭션 종료시간. */
    @Setter
    private LocalDateTime transactionEndTime;

    /** 트랜잭션 실행시간(ms). */
    @Setter
    private long transactionDurationMs;

    /** 성공 여부. */
    @Setter
    private boolean success;

    /** 에러 메시지 요약. */
    @Setter
    private String errorMessage;

    /** 실행 SQL 목록. */
    @Builder.Default
    private List<QueryTraceSql> sqls = new ArrayList<>();

    /**
     * SQL 정보 추가.
     *
     * @param sql SQL 정보
     */
    public void addSql(QueryTraceSql sql) {
        this.sqls.add(sql);
    }

    /**
     * 다음 SQL 순번 반환.
     *
     * @return 다음 SQL 순번
     */
    public int nextSqlOrderNo() {
        return this.sqls.size() + 1;
    }

    /**
     * SQL 개수 반환.
     *
     * @return SQL 개수
     */
    public int getQueryCount() {
        return this.sqls == null ? 0 : this.sqls.size();
    }
}
