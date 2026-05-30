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
 * 엑셀 다운로드 요청 DTO.
 *
 * <p>
 * 서버에서 Apache POI를 이용해 엑셀 파일을 생성할 때 사용하는 요청 구조이다.
 * </p>
 *
 * <p>
 * 사용 시나리오:
 * 1. 현재 Grid 데이터 서버 다운로드
 * 2. 전체 데이터 다운로드
 * 3. 동적 컬럼 다운로드
 * 4. 다중 헤더 다운로드
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDownloadRequest {

    /**
     * 다운로드 파일명.
     *
     * <p>
     * 예:
     * sales-list.xlsx,
     * customer-template.xlsx
     * </p>
     */
    private String fileName;

    /**
     * 시트명.
     *
     * <p>
     * 단일 시트 다운로드에서는 이 값을 사용한다.
     * 다중 시트 다운로드에서는 sheets 안의 sheetName을 우선 사용한다.
     * </p>
     */
    private String sheetName;

    /**
     * 컬럼 메타 목록.
     *
     * <p>
     * AG Grid columnDefs를 서버 요청용으로 변환한 구조이다.
     * </p>
     *
     * <p>
     * 다운로드 시 이 컬럼 순서대로 헤더와 데이터를 생성한다.
     * hidden=true인 컬럼은 다운로드에서 제외할 수 있다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 다운로드할 데이터 목록.
     *
     * <p>
     * 동적 컬럼을 지원하기 위해 DTO가 아니라 Map 구조를 사용한다.
     * </p>
     *
     * <p>
     * key는 ExcelColumnMeta.field와 매칭된다.
     * </p>
     */
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 다중 시트 다운로드 요청 목록.
     *
     * <p>
     * 1차 구현에서는 단일 시트를 우선 처리하되,
     * 확장성을 위해 다중 시트 구조를 열어둔다.
     * </p>
     */
    @Builder.Default
    private List<ExcelSheetRequest> sheets = new ArrayList<>();

    /**
     * 예제 행 포함 여부.
     *
     * <p>
     * 템플릿 다운로드 또는 샘플 다운로드에서 사용한다.
     * true이면 ExcelColumnMeta.exampleValue 기준으로 예제 1행을 생성한다.
     * </p>
     */
    private boolean includeExampleRow;

    /**
     * 숨김 컬럼 제외 여부.
     *
     * <p>
     * true이면 ExcelColumnMeta.hidden=true 컬럼은 다운로드 대상에서 제외한다.
     * </p>
     */
    private boolean excludeHiddenColumns;

    /**
     * 다중 헤더 사용 여부.
     *
     * <p>
     * true이면 parentHeader, level 정보를 기준으로 병합 헤더를 생성한다.
     * </p>
     */
    private boolean useMultiHeader;

    /**
     * 전체 다운로드 여부.
     *
     * <p>
     * true이면 rows를 직접 사용하지 않고,
     * 조회조건(searchCondition)을 기준으로 서버에서 전체 데이터를 재조회하는 방식으로 사용할 수 있다.
     * </p>
     */
    private boolean fullDownload;

    /**
     * 조회조건.
     *
     * <p>
     * 전체 데이터 다운로드 시 사용한다.
     * 화면의 검색 조건을 Map 형태로 전달한다.
     * </p>
     *
     * <p>
     * 예:
     * startYm=202501,
     * endYm=202512,
     * customerCode=C001
     * </p>
     */
    private Map<String, Object> searchCondition;
}