package com.example.demo.excel.sample.dynamicgrid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 다이나믹 그리드 MyBatis 조회 결과 DTO.
 *
 * <p>
 * DB에는 동적 컬럼 값이 세로형으로 저장된다.
 * MyBatis 조회 결과도 세로형 row로 받은 뒤,
 * Service에서 AG Grid용 가로형 Map으로 변환한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDynamicGridRow {

    /**
     * 기준년월.
     */
    private String baseYm;

    /**
     * 라디오 선택값.
     */
    private String radioType;

    /**
     * 다중 선택 조건값.
     */
    private String multiType;

    /**
     * 단건 선택 조건값.
     */
    private String versionCode;

    /**
     * 기본 컬럼: 구분.
     */
    private String categoryName;

    /**
     * 기본 컬럼: APP.
     */
    private String appName;

    /**
     * 기본 컬럼: GA/NGA.
     */
    private String gaNgaType;

    /**
     * 기본 컬럼: CUSTOMER.
     */
    private String customerName;

    /**
     * 계층 스타일 플래그 3.
     */
    private Integer sort3;

    /**
     * 계층 스타일 플래그 4.
     */
    private Integer sort4;

    /**
     * 계층 스타일 플래그 5.
     */
    private Integer sort5;

    /**
     * Dimension 타입.
     *
     * <p>
     * 예:
     * QTY,
     * ASP,
     * AMT,
     * Ratio
     * </p>
     */
    private String metricType;

    /**
     * 연도.
     *
     * <p>
     * 예:
     * 2026
     * </p>
     */
    private String yearValue;

    /**
     * 분기 또는 월/TOTAL 값.
     *
     * <p>
     * 예:
     * 01,
     * 02,
     * 03,
     * TOT
     * </p>
     */
    private String quarterValue;

    /**
     * 실제 값.
     */
    private BigDecimal valueDecimal;
}