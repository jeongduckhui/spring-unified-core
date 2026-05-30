package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 엑셀 컬럼 메타 정보 DTO.
 *
 * <p>
 * 이 클래스는 엑셀 다운로드, 템플릿 다운로드, 업로드 헤더 검증에서
 * 공통으로 사용하는 컬럼 정의 정보이다.
 * </p>
 *
 * <p>
 * AG Grid의 columnDefs와 비슷한 역할을 하며,
 * 화면 컬럼 정보를 서버로 전달할 때 이 구조로 변환해서 사용한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelColumnMeta {

    /**
     * 데이터 필드명.
     *
     * <p>
     * 예:
     * customerCode,
     * customerName,
     * qty,
     * amt,
     * 2025_Q1,
     * 2025_QTY
     * </p>
     *
     * <p>
     * 다운로드 시에는 rowData Map의 key로 사용하고,
     * 업로드 시에는 엑셀 헤더를 이 field로 매핑한다.
     * </p>
     */
    private String field;

    /**
     * 화면 또는 엑셀에 표시할 헤더명.
     *
     * <p>
     * 예:
     * 고객코드,
     * 고객명,
     * 수량,
     * 금액,
     * 1Q,
     * QTY
     * </p>
     */
    private String headerName;

    /**
     * 상위 헤더명.
     *
     * <p>
     * 다중 헤더가 있는 경우 사용한다.
     * 예:
     * 2025 > 1Q
     * 2026 > QTY
     * </p>
     *
     * <p>
     * 단일 헤더 컬럼이면 null 또는 빈 값으로 둔다.
     * </p>
     */
    private String parentHeader;

    /**
     * 헤더 레벨.
     *
     * <p>
     * 단일 헤더이면 1,
     * 2단 헤더이면 상위/하위 구조 기준으로 2,
     * 3단 헤더까지 확장할 경우 3으로 사용할 수 있다.
     * </p>
     */
    private Integer level;

    /**
     * 3단 이상 다중 헤더 경로.
     *
     * <p>
     * 예:
     * ["QTY", "2026", "1M"]
     * ["AMT", "2026", "Total"]
     * </p>
     *
     * <p>
     * 이 값이 있으면 ExcelDownloadService는 parentHeader/headerName이 아니라
     * headerPath 기준으로 헤더 row와 병합을 생성한다.
     * </p>
     */
    @Builder.Default
    private List<String> headerPath = new ArrayList<>();

    /**
     * 필수 컬럼 여부.
     *
     * <p>
     * true이면 템플릿 다운로드 시 헤더 배경색을 강조하고,
     * 업로드 시 해당 컬럼이 누락되면 오류로 처리한다.
     * </p>
     */
    private boolean required;

    /**
     * 예제 값.
     *
     * <p>
     * 템플릿 다운로드 시 예제 데이터 1행을 생성할 때 사용한다.
     * 예:
     * C001,
     * 삼성전자,
     * 100,
     * 2026-01-01
     * </p>
     */
    private String exampleValue;

    /**
     * 컬럼 데이터 타입.
     *
     * <p>
     * 업로드 시 셀 값을 어떤 타입으로 변환할지 판단하는 기준이다.
     * 예:
     * STRING,
     * INTEGER,
     * LONG,
     * BIG_DECIMAL,
     * LOCAL_DATE,
     * LOCAL_DATE_TIME
     * </p>
     */
    @Builder.Default
    private ExcelCellDataType dataType = ExcelCellDataType.STRING;

    /**
     * 숨김 컬럼 여부.
     *
     * <p>
     * true이면 다운로드 대상에서 제외할 수 있다.
     * AG Grid의 hide 속성과 매핑하기 좋다.
     * </p>
     */
    private boolean hidden;

    /**
     * 컬럼 순서.
     *
     * <p>
     * 프론트에서 전달된 컬럼 순서를 서버에서 안정적으로 유지하기 위해 사용한다.
     * </p>
     */
    private Integer order;
}