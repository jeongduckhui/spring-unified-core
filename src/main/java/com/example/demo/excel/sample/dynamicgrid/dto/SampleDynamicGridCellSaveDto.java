package com.example.demo.excel.sample.dynamicgrid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 다이나믹 그리드 저장용 세로형 셀 DTO.
 *
 * <p>
 * 프론트에서는 동적 컬럼이 가로 형태로 넘어온다.
 * 서버 저장 전에는 이 DTO 구조로 세로형 데이터로 변환한다.
 * </p>
 *
 * <p>
 * 예:
 * QTY_Q202601 = 100
 *
 * 변환 후:
 * metricType = QTY
 * yearValue = 2026
 * quarterValue = 01
 * valueDecimal = 100
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDynamicGridCellSaveDto {

    /**
     * row 상태.
     *
     * <p>
     * I: 신규
     * U: 수정
     * D: 삭제
     * </p>
     */
    private String rowStatus;

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
     */
    private String metricType;

    /**
     * 연도.
     */
    private String yearValue;

    /**
     * 분기 또는 TOTAL.
     */
    private String quarterValue;

    /**
     * 저장 값.
     */
    private BigDecimal valueDecimal;
}