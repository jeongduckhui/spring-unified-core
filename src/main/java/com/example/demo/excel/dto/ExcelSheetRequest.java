package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 엑셀 시트 요청 DTO.
 *
 * <p>
 * 하나의 엑셀 파일 안에 여러 개의 Sheet를 생성해야 할 때 사용한다.
 * </p>
 *
 * <p>
 * 1차 구현에서는 단일 시트를 우선 처리하지만,
 * 이후 다중 시트 다운로드를 위해 구조를 분리해둔다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSheetRequest {

    /**
     * 시트명.
     *
     * <p>
     * 예:
     * Sales,
     * Customer,
     * ErrorRows
     * </p>
     */
    private String sheetName;

    /**
     * 시트 컬럼 메타 목록.
     *
     * <p>
     * 이 시트에서 사용할 컬럼 정의이다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 시트 데이터 목록.
     *
     * <p>
     * 동적 컬럼 지원을 위해 Map 구조를 사용한다.
     * </p>
     */
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 예제 행 포함 여부.
     *
     * <p>
     * 템플릿성 시트에서 예제 데이터를 넣을지 결정한다.
     * </p>
     */
    private boolean includeExampleRow;

    /**
     * 숨김 컬럼 제외 여부.
     */
    private boolean excludeHiddenColumns;

    /**
     * 다중 헤더 사용 여부.
     */
    private boolean useMultiHeader;
}