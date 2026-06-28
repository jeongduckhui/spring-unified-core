package com.example.demo.querytrace.sample.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 쿼리추적 샘플 조회조건 DTO.
 */
@Getter
@Setter
public class QueryTraceSampleSearchRequest {

    private String startYm;
    private String endYm;
    private String viewType;
    private String multiType;
    private String versionCode;
    private String customerName;
    private String metricType;
}
