package com.example.demo.excel.sample.dynamicgrid.dto;

import com.example.demo.excel.dto.ExcelColumnMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 다이나믹 그리드 조회 요청 DTO.
 *
 * <p>
 * 이 DTO는 실무형 AG Grid + Excel 샘플 화면의 조회조건을 담는다.
 * </p>
 *
 * <p>
 * 조회조건:
 * 1. 시작년월
 * 2. 종료년월
 * 3. 라디오 단일 선택값
 * 4. 체크박스형 다중 선택값
 * 5. 단건 선택 셀렉트박스 값
 * 6. 선택 Dimension 목록
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDynamicGridSearchRequest {

    /**
     * 시작년월.
     *
     * <p>
     * 예:
     * 202601
     * </p>
     */
    private String startYm;

    /**
     * 종료년월.
     *
     * <p>
     * 예:
     * 202612
     * </p>
     */
    private String endYm;

    /**
     * 라디오 버튼 선택값.
     *
     * <p>
     * 예:
     * PLAN,
     * ACTUAL
     * </p>
     */
    private String radioType;

    /**
     * 다중 선택 셀렉트박스 값 목록.
     *
     * <p>
     * 예:
     * DOMESTIC,
     * EXPORT
     * </p>
     */
    @Builder.Default
    private List<String> multiSelectValues = new ArrayList<>();

    /**
     * 단건 선택 셀렉트박스 값.
     *
     * <p>
     * 예:
     * V1,
     * V2
     * </p>
     */
    private String singleSelectValue;

    /**
     * 선택된 Dimension 목록.
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
    @Builder.Default
    private List<String> dimensions = new ArrayList<>();

    /**
     * 엑셀 전체 다운로드 시 사용할 컬럼 메타 목록.
     *
     * <p>
     * 전체 다운로드에서는 프론트가 현재 화면 컬럼 구조를 서버로 전달한다.
     * 서버는 이 컬럼 메타 기준으로 엑셀 헤더를 생성한다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 숨김 컬럼 제외 여부.
     */
    private boolean excludeHiddenColumns;

    /**
     * 다중 헤더 사용 여부.
     */
    private boolean useMultiHeader;
}