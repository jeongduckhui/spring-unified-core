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
 * 엑셀 오류 행 다운로드 요청 DTO.
 *
 * <p>
 * 엑셀 업로드 결과 중 오류 행만 다시 엑셀로 다운로드할 때 사용한다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelErrorRowsDownloadRequest {

    /**
     * 다운로드 파일명.
     */
    private String fileName;

    /**
     * 시트명.
     */
    private String sheetName;

    /**
     * 컬럼 메타 목록.
     *
     * <p>
     * 업로드/다운로드에 사용한 기존 ColumnMeta를 그대로 전달한다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 업로드 결과 rows.
     *
     * <p>
     * ExcelUploadResult.rows를 그대로 전달한다.
     * 이 rows 중 _rowStatus = ERROR 인 행만 필터링해서 엑셀로 생성한다.
     * </p>
     */
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 숨김 컬럼 제외 여부.
     */
    private boolean excludeHiddenColumns;

    /**
     * 다중 헤더 사용 여부.
     */
    private boolean useMultiHeader;

    /**
     * 오류 메시지 컬럼 포함 여부.
     *
     * <p>
     * true이면 마지막 컬럼에 오류 메시지 컬럼을 추가한다.
     * </p>
     */
    @Builder.Default
    private boolean includeErrorMessageColumn = true;
}