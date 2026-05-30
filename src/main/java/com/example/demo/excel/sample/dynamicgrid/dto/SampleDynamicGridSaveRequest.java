package com.example.demo.excel.sample.dynamicgrid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 다이나믹 그리드 저장 요청 DTO.
 *
 * <p>
 * 프론트 AG Grid의 rowData를 저장 API로 전달할 때 사용한다.
 * </p>
 *
 * <p>
 * rowData는 동적 컬럼을 포함하므로 DTO 고정 필드가 아니라
 * List&lt;Map&lt;String, Object&gt;&gt; 구조로 받는다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDynamicGridSaveRequest {

    /**
     * 시작년월.
     *
     * <p>
     * 저장 시 동적 컬럼 해석 범위에 사용한다.
     * </p>
     */
    private String startYm;

    /**
     * 종료년월.
     */
    private String endYm;

    /**
     * 라디오 버튼 선택값.
     */
    private String radioType;

    /**
     * 다중 선택 셀렉트박스 값 목록.
     */
    @Builder.Default
    private List<String> multiSelectValues = new ArrayList<>();

    /**
     * 단건 선택 셀렉트박스 값.
     */
    private String singleSelectValue;

    /**
     * 선택된 Dimension 목록.
     */
    @Builder.Default
    private List<String> dimensions = new ArrayList<>();

    /**
     * 저장 대상 rowData.
     *
     * <p>
     * 예:
     * {
     *   "_rowStatus": "U",
     *   "categoryName": "구분1",
     *   "appName": "APP1",
     *   "gaNgaType": "GA",
     *   "customerName": "삼성전자",
     *   "QTY_Q202601": 100
     * }
     * </p>
     */
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();
}