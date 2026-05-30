package com.example.demo.excel.sample.dynamicgrid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 엑셀 샘플 다이나믹 그리드 Entity.
 *
 * <p>
 * 이 Entity는 실무형 AG Grid + Excel 업로드/다운로드 샘플 데이터를 저장한다.
 * </p>
 *
 * <p>
 * 화면에서는 동적 컬럼이 가로 형태로 보이지만,
 * DB에는 세로형 구조로 저장한다.
 * </p>
 *
 * <p>
 * 예:
 * 화면 컬럼: QTY_Q202601 = 100
 *
 * DB 저장:
 * metricType = QTY
 * yearValue = 2026
 * quarterValue = 01
 * valueDecimal = 100
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "SAMPLE_DYNAMIC_GRID")
public class SampleDynamicGridEntity {

    /**
     * PK.
     *
     * <p>
     * 샘플에서는 단순 IDENTITY 전략을 사용한다.
     * Oracle 운영 환경에서는 시퀀스 전략으로 바꿀 수 있다.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * 기준년월.
     *
     * <p>
     * 예:
     * 202601
     * </p>
     */
    @Column(name = "BASE_YM", nullable = false, length = 6)
    private String baseYm;

    /**
     * 라디오 선택값.
     *
     * <p>
     * 예:
     * PLAN,
     * ACTUAL
     * </p>
     */
    @Column(name = "VIEW_TYPE", length = 20)
    private String radioType;

    /**
     * 다중 선택 조건값.
     *
     * <p>
     * 다중 선택값은 저장 편의를 위해 row 단위에서는 하나의 값으로 저장한다.
     * 예:
     * DOMESTIC,
     * EXPORT
     * </p>
     */
    @Column(name = "MULTI_TYPE", length = 50)
    private String multiType;

    /**
     * 단건 선택 조건값.
     *
     * <p>
     * 예:
     * V1,
     * V2
     * </p>
     */
    @Column(name = "VERSION_CODE", length = 50)
    private String versionCode;

    /**
     * 기본 컬럼: 구분.
     */
    @Column(name = "CATEGORY_NAME", length = 100)
    private String categoryName;

    /**
     * 기본 컬럼: APP.
     */
    @Column(name = "APP_NAME", length = 100)
    private String appName;

    /**
     * 기본 컬럼: GA/NGA.
     */
    @Column(name = "GA_NGA_TYPE", length = 50)
    private String gaNgaType;

    /**
     * 기본 컬럼: CUSTOMER.
     */
    @Column(name = "CUSTOMER_NAME", length = 100)
    private String customerName;

    /**
     * 계층 스타일 플래그 3.
     *
     * <p>
     * 1,1,1: 최상위
     * 0,1,1: 중간
     * 0,0,0: 하위
     * </p>
     */
    @Column(name = "SORT3")
    private Integer sort3;

    /**
     * 계층 스타일 플래그 4.
     */
    @Column(name = "SORT4")
    private Integer sort4;

    /**
     * 계층 스타일 플래그 5.
     */
    @Column(name = "SORT5")
    private Integer sort5;

    /**
     * Dimension 타입.
     *
     * <p>
     * 예:
     * QTY,
     * ASP,
     * AMT,
     * Quarter,
     * Ratio
     * </p>
     */
    @Column(name = "METRIC_TYPE", nullable = false, length = 20)
    private String metricType;

    /**
     * 연도.
     *
     * <p>
     * 예:
     * 2026
     * </p>
     */
    @Column(name = "YEAR_VALUE", nullable = false, length = 4)
    private String yearValue;

    /**
     * 분기 또는 TOTAL 값.
     *
     * <p>
     * 예:
     * 01,
     * 02,
     * 03,
     * 04,
     * TOT
     * </p>
     */
    @Column(name = "QUARTER_VALUE", nullable = false, length = 10)
    private String quarterValue;

    /**
     * 실제 값.
     *
     * <p>
     * Oracle NUMBER(22, 10) 또는 H2 DECIMAL(22, 10)에 대응한다.
     * </p>
     */
    @Column(name = "VALUE_DECIMAL", precision = 22, scale = 10)
    private BigDecimal valueDecimal;

    /**
     * 생성일시.
     */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /**
     * 수정일시.
     */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /**
     * 저장 전 생성/수정일시를 세팅한다.
     */
    @PrePersist
    protected void prePersist() {

        // 현재 시간을 구한다.
        LocalDateTime now = LocalDateTime.now();

        // 생성일시를 세팅한다.
        this.createdAt = now;

        // 수정일시를 세팅한다.
        this.updatedAt = now;
    }

    /**
     * 수정 전 수정일시를 세팅한다.
     */
    @PreUpdate
    protected void preUpdate() {

        // 수정일시를 현재 시간으로 갱신한다.
        this.updatedAt = LocalDateTime.now();
    }
}