package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 엑셀 템플릿 다운로드 요청 DTO.
 *
 * <p>
 * 현재 화면 컬럼 기준으로 업로드용 템플릿 파일을 생성할 때 사용한다.
 * </p>
 *
 * <p>
 * 고정 xlsx 파일을 서버에 보관하지 않고,
 * 화면에서 전달한 컬럼 메타 정보를 기준으로 템플릿을 자동 생성한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelTemplateDownloadRequest {

    /**
     * 다운로드 파일명.
     *
     * <p>
     * 예:
     * sales-upload-template.xlsx
     * </p>
     */
    private String fileName;

    /**
     * 시트명.
     *
     * <p>
     * 예:
     * 업로드양식,
     * SalesTemplate
     * </p>
     */
    private String sheetName;

    /**
     * 템플릿 컬럼 메타 목록.
     *
     * <p>
     * 이 정보로 헤더를 생성한다.
     * required=true이면 헤더 배경색을 강조한다.
     * exampleValue가 있으면 예제 행 생성 시 사용한다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 예제 행 포함 여부.
     *
     * <p>
     * true이면 ExcelColumnMeta.exampleValue를 사용해 예제 데이터 1행을 생성한다.
     * </p>
     */
    private boolean includeExampleRow;

    /**
     * 숨김 컬럼 제외 여부.
     *
     * <p>
     * true이면 hidden=true 컬럼은 템플릿에서 제외한다.
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
}