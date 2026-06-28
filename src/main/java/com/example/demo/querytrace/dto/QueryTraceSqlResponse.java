package com.example.demo.querytrace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 쿼리보기 SQL 상세 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTraceSqlResponse {

    private int orderNo;
    private String mapperId;
    private String sqlCommandType;
    private String sql;
    private String executionStartTime;
    private String executionEndTime;
    private long executionTimeMs;
}
