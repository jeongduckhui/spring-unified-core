package com.example.demo.querytrace.sample.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAMPLE_DYNAMIC_GRID 조회 결과 DTO.
 */
@Getter
@Setter
public class QueryTraceSampleRow {

    private Long id;
    private String baseYm;
    private String viewType;
    private String multiType;
    private String versionCode;
    private String categoryName;
    private String appName;
    private String gaNgaType;
    private String customerName;
    private Integer sort3;
    private Integer sort4;
    private Integer sort5;
    private String metricType;
    private String yearValue;
    private String quarterValue;
    private BigDecimal valueDecimal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
