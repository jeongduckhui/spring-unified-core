package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 엑셀 다중 헤더 그룹 메타 정보 DTO.
 *
 * <p>
 * Apache POI에서 셀 병합을 생성할 때 사용하는 정보이다.
 * </p>
 *
 * <p>
 * 예:
 * 2025 헤더가 1Q, 2Q, 3Q, 4Q 하위 컬럼을 가진다면
 * startColumnIndex = 4,
 * endColumnIndex = 7
 * 형태로 표현할 수 있다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelHeaderGroupMeta {

    /**
     * 상위 헤더명.
     *
     * <p>
     * 예:
     * 2025,
     * 2026,
     * Dimension(QTY)
     * </p>
     */
    private String headerName;

    /**
     * 헤더 레벨.
     *
     * <p>
     * 1단 상위 헤더인지,
     * 2단 상위 헤더인지,
     * 3단 헤더인지 구분한다.
     * </p>
     */
    private Integer level;

    /**
     * 병합 시작 컬럼 인덱스.
     *
     * <p>
     * Apache POI 기준으로 0부터 시작한다.
     * </p>
     */
    private Integer startColumnIndex;

    /**
     * 병합 종료 컬럼 인덱스.
     *
     * <p>
     * Apache POI 기준으로 0부터 시작한다.
     * </p>
     */
    private Integer endColumnIndex;

    /**
     * 병합 시작 행 인덱스.
     *
     * <p>
     * Apache POI 기준으로 0부터 시작한다.
     * </p>
     */
    private Integer startRowIndex;

    /**
     * 병합 종료 행 인덱스.
     *
     * <p>
     * Apache POI 기준으로 0부터 시작한다.
     * </p>
     */
    private Integer endRowIndex;
}