package com.example.demo.common.logging;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * SQL 로그 포맷 Formatter
 *
 * P6Spy에서 출력하는 SQL 로그를
 * 가독성 좋은 형태로 변환하는 역할을 수행한다.
 *
 * Formatter 역할
 *
 * 1 SQL 공백 정리
 * 2 SQL 실행 시간 출력
 * 3 SQL 카테고리 출력
 * 4 Connection ID 출력
 * 5 SQL 문 출력
 *
 * 출력 예
 *
 * 12 ms | statement | connection 1 | select * from users where id = 1
 *
 * 사용 목적
 *
 * 기본 P6Spy 로그는 줄바꿈이 많고
 * 로그가 길어지기 때문에
 * 운영 로그에서 가독성이 떨어질 수 있다.
 *
 * 본 Formatter는 SQL을 한 줄로 정리하여
 * 로그 파일에서 빠르게 조회할 수 있도록 한다.
 *
 * 적용 위치
 *
 * p6spy 설정
 *
 * modulelist=com.p6spy.engine.spy.P6SpyFactory
 * logMessageFormat=com.example.demo.common.logging.PrettySqlFormatter
 *
 * 로그 파일
 *
 * sql.log
 */
public class PrettySqlFormatter implements MessageFormattingStrategy {

    /**
     * SQL 로그 포맷 처리 메서드
     *
     * P6Spy가 SQL 실행 시 해당 메서드를 호출한다.
     */
    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url) {

        /**
         * SQL이 없는 경우 로그 출력하지 않음
         */
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        /**
         * SQL 공백 정리
         *
         * 여러 줄 SQL을 한 줄로 변환하여
         * 로그 가독성을 개선한다.
         */
        String formattedSql = sql
                .replaceAll("\\s+", " ")
                .trim();

        /**
         * SQL 로그 출력 형식
         *
         * 실행시간(ms)
         * SQL 카테고리
         * connection id
         * SQL
         */
        return elapsed + " ms | " + category + " | connection " + connectionId + " | " + formattedSql;
    }
}