package com.example.demo.querytrace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 쿼리보기 목록 응답 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTraceMetaResponse {

    private String traceId;
    private String userId;
    private String screenId;
    private String requestUri;
    private String controllerName;
    private String methodName;
    private String description;
    private String transactionStartTime;
    private String transactionEndTime;
    private long transactionDurationMs;
    private int queryCount;
    private boolean success;
    private String errorMessage;
    private String createdAt;
}
