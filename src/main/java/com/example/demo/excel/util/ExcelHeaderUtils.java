package com.example.demo.excel.util;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelUploadOption;
import com.example.demo.excel.dto.ExcelValidationError;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 엑셀 헤더 처리 유틸 클래스.
 *
 * <p>
 * 업로드된 엑셀 파일의 헤더를 읽고,
 * ColumnMeta 기준으로 헤더 검증과 컬럼 매핑을 수행한다.
 * </p>
 */
@Slf4j
public final class ExcelHeaderUtils {

    /**
     * 다중 헤더 조합 구분자.
     *
     * <p>
     * 예:
     * 2025 + 1Q = 2025_1Q
     * </p>
     */
    private static final String HEADER_JOIN_DELIMITER = "_";

    /**
     * 유틸 클래스이므로 외부에서 생성하지 못하게 막는다.
     */
    private ExcelHeaderUtils() {
    }

    /**
     * 업로드 엑셀의 헤더 정보를 읽어 컬럼 인덱스별 헤더 key를 생성한다.
     *
     * @param sheet 엑셀 시트
     * @param option 업로드 옵션
     * @return 컬럼 인덱스별 헤더 key Map
     */
    public static Map<Integer, String> readHeaderMap(
            Sheet sheet,
            ExcelUploadOption option
    ) {

        // 단일 헤더이면 단일 헤더 Map을 생성한다.
        if (!option.isUseMultiHeader()) {
            return readSingleHeaderMap(sheet, option.getHeaderStartRowIndex());
        }

        log.info("header row start={}", option.getHeaderStartRowIndex());
        log.info("header row end={}", option.getHeaderEndRowIndex());

        // 다중 헤더이면 다중 헤더 Map을 생성한다.
        return readMultiHeaderMap(
                sheet,
                option.getHeaderStartRowIndex(),
                option.getHeaderEndRowIndex()
        );
    }

    /**
     * 단일 헤더 Map을 생성한다.
     *
     * @param sheet 엑셀 시트
     * @param headerRowIndex 헤더 row index
     * @return 컬럼 인덱스별 헤더명 Map
     */
    private static Map<Integer, String> readSingleHeaderMap(
            Sheet sheet,
            int headerRowIndex
    ) {

        // 결과 Map. 순서 유지를 위해 LinkedHashMap을 사용한다.
        Map<Integer, String> headerMap = new LinkedHashMap<>();

        // 헤더 행을 가져온다.
        Row headerRow = sheet.getRow(headerRowIndex);

        // 헤더 행이 없으면 빈 Map 반환.
        if (headerRow == null) {
            return headerMap;
        }

        // 마지막 셀 번호까지 순회한다.
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {

            // 셀 값을 문자열로 읽는다.
//            String headerName = ExcelCellReadUtils.readAsString(headerRow.getCell(cellIndex));
            String headerName =
                    getHeaderValue(
                            sheet,
                            headerRowIndex,
                            cellIndex
                    );

            // 헤더명이 있으면 Map에 추가한다.
            if (StringUtils.hasText(headerName)) {
                headerMap.put(cellIndex, normalizeHeaderName(headerName));
            }
        }

        // 헤더 Map 반환.
        return headerMap;
    }

    /**
     * 다중 헤더 Map을 생성한다.
     *
     * <p>
     * 여러 헤더 row의 값을 조합해서 하나의 key로 만든다.
     * </p>
     *
     * <p>
     * 예:
     * 2025 / 1Q → 2025_1Q
     * </p>
     *
     * @param sheet 엑셀 시트
     * @param headerStartRowIndex 헤더 시작 row index
     * @param headerEndRowIndex 헤더 종료 row index
     * @return 컬럼 인덱스별 조합 헤더 key Map
     */
    private static Map<Integer, String> readMultiHeaderMap(
            Sheet sheet,
            int headerStartRowIndex,
            int headerEndRowIndex
    ) {

        // 결과 Map. 순서 유지를 위해 LinkedHashMap을 사용한다.
        Map<Integer, String> headerMap = new LinkedHashMap<>();

        // 최대 셀 개수를 구한다.
        int maxCellNum = findMaxCellNum(sheet, headerStartRowIndex, headerEndRowIndex);

        // 컬럼 인덱스 기준으로 헤더 row들을 조합한다.
        for (int cellIndex = 0; cellIndex < maxCellNum; cellIndex++) {

            // 헤더 조각 목록.
            List<String> parts = new ArrayList<>();

            // 헤더 row 범위를 순회한다.
            for (int rowIndex = headerStartRowIndex; rowIndex <= headerEndRowIndex; rowIndex++) {

                // 현재 row를 가져온다.
                Row row = sheet.getRow(rowIndex);

                // row가 없으면 다음 row로 넘어간다.
                if (row == null) {
                    continue;
                }

                // 셀 값을 문자열로 읽는다.
                String headerPart = ExcelCellReadUtils.readAsString(row.getCell(cellIndex));

                // 헤더 조각이 있으면 추가한다.
                if (StringUtils.hasText(headerPart)) {
//                    parts.add(normalizeHeaderName(headerPart));
                    // 직전 값과 동일하면 추가하지 않는다.
                    if (parts.isEmpty()
                            || !parts.get(parts.size() - 1).equals(headerPart)) {

                        parts.add(normalizeHeaderName(headerPart));
                    }
                }
            }

            // 헤더 조각이 있으면 구분자로 합쳐서 Map에 넣는다.
            if (!parts.isEmpty()) {
                headerMap.put(cellIndex, String.join(HEADER_JOIN_DELIMITER, parts));
            }
        }

        // 헤더 Map 반환.
        return headerMap;
    }

    private static String getHeaderValue(
            Sheet sheet,
            int rowIndex,
            int cellIndex
    ) {

        Row row = sheet.getRow(rowIndex);

        if (row == null) {
            return "";
        }

        String value =
                ExcelCellReadUtils.readAsString(
                        row.getCell(cellIndex)
                );

        if (StringUtils.hasText(value)) {
            return value;
        }

        for (var mergedRegion : sheet.getMergedRegions()) {

            if (mergedRegion.isInRange(rowIndex, cellIndex)) {

                Row firstRow =
                        sheet.getRow(
                                mergedRegion.getFirstRow()
                        );

                if (firstRow == null) {
                    return "";
                }

                return ExcelCellReadUtils.readAsString(
                        firstRow.getCell(
                                mergedRegion.getFirstColumn()
                        )
                );
            }
        }

        return "";
    }

    /**
     * ColumnMeta 기준의 기대 헤더 key를 생성한다.
     *
     * @param column 컬럼 메타
     * @param useMultiHeader 다중 헤더 여부
     * @return 기대 헤더 key
     */
    public static String createExpectedHeaderKey(
            ExcelColumnMeta column,
            boolean useMultiHeader
    ) {

        if (useMultiHeader
                && column.getHeaderPath() != null
                && !column.getHeaderPath().isEmpty()) {

            return column.getHeaderPath().stream()
                    .filter(StringUtils::hasText)
                    .map(ExcelHeaderUtils::normalizeHeaderName)
                    .reduce((a, b) -> a + HEADER_JOIN_DELIMITER + b)
                    .orElse("");
        }

        if (useMultiHeader && StringUtils.hasText(column.getParentHeader())) {
            return normalizeHeaderName(column.getParentHeader())
                    + HEADER_JOIN_DELIMITER
                    + normalizeHeaderName(column.getHeaderName());
        }

        return normalizeHeaderName(column.getHeaderName());
    }
    /*
    public static String createExpectedHeaderKey(
            ExcelColumnMeta column,
            boolean useMultiHeader
    ) {

        // 다중 헤더이고 parentHeader가 있으면 parentHeader_headerName 조합을 사용한다.
        if (useMultiHeader && StringUtils.hasText(column.getParentHeader())) {
            return normalizeHeaderName(column.getParentHeader())
                    + HEADER_JOIN_DELIMITER
                    + normalizeHeaderName(column.getHeaderName());
        }

        // 그 외에는 headerName만 사용한다.
        return normalizeHeaderName(column.getHeaderName());
    }
    */

    /**
     * 업로드 헤더를 기준으로 field 매핑 정보를 생성한다.
     *
     * <p>
     * 반환 구조:
     * key   = 엑셀 컬럼 index
     * value = ExcelColumnMeta
     * </p>
     *
     * @param uploadHeaderMap 업로드 파일의 컬럼 인덱스별 헤더 key
     * @param columns 기대 컬럼 메타 목록
     * @param useMultiHeader 다중 헤더 여부
     * @return 엑셀 컬럼 index별 ColumnMeta 매핑
     */
    public static Map<Integer, ExcelColumnMeta> createColumnIndexMap(
            Map<Integer, String> uploadHeaderMap,
            List<ExcelColumnMeta> columns,
            boolean useMultiHeader
    ) {

        // 기대 헤더 key -> ColumnMeta Map.
        Map<String, ExcelColumnMeta> expectedHeaderMap = new LinkedHashMap<>();

        // 기대 컬럼 목록을 순회한다.
        for (ExcelColumnMeta column : columns) {

            // 기대 헤더 key를 생성한다.
            String expectedHeaderKey = createExpectedHeaderKey(column, useMultiHeader);

            log.info(
                    "expectedHeaderKey={}",
                    expectedHeaderKey
            );

            // 기대 헤더 Map에 추가한다.
            expectedHeaderMap.put(expectedHeaderKey, column);
        }

        // 결과 Map. 엑셀 컬럼 index -> ColumnMeta.
        Map<Integer, ExcelColumnMeta> columnIndexMap = new LinkedHashMap<>();

        // 업로드 헤더 Map을 순회한다.
        for (Map.Entry<Integer, String> entry : uploadHeaderMap.entrySet()) {

            // 업로드 헤더 key.
            String uploadHeaderKey = entry.getValue();

            // 기대 헤더에 존재하는 경우만 매핑한다.
            if (expectedHeaderMap.containsKey(uploadHeaderKey)) {
                columnIndexMap.put(entry.getKey(), expectedHeaderMap.get(uploadHeaderKey));
            }
        }

        // 컬럼 index 매핑 반환.
        return columnIndexMap;
    }

    /**
     * 헤더 검증 오류 목록을 생성한다.
     *
     * <p>
     * 필수 컬럼이 업로드 파일에 존재하지 않으면 오류로 반환한다.
     * </p>
     *
     * @param uploadHeaderMap 업로드 파일 헤더 Map
     * @param columns 기대 컬럼 목록
     * @param useMultiHeader 다중 헤더 여부
     * @return 헤더 검증 오류 목록
     */
    public static List<ExcelValidationError> validateHeaders(
            Map<Integer, String> uploadHeaderMap,
            List<ExcelColumnMeta> columns,
            boolean useMultiHeader
    ) {

        // 오류 목록.
        List<ExcelValidationError> errors = new ArrayList<>();

        // 업로드 헤더 key 목록.
        List<String> uploadHeaderKeys = new ArrayList<>(uploadHeaderMap.values());

        // 기대 컬럼을 순회한다.
        for (ExcelColumnMeta column : columns) {

            // 기대 헤더 key를 생성한다.
            String expectedHeaderKey = createExpectedHeaderKey(column, useMultiHeader);

            // 필수 컬럼인데 업로드 파일에 없으면 오류로 추가한다.
            if (column.isRequired() && !uploadHeaderKeys.contains(expectedHeaderKey)) {
                errors.add(ExcelValidationError.builder()
                        .rowNo(0)
                        .field(column.getField())
                        .headerName(column.getHeaderName())
                        .message("필수 컬럼이 업로드 파일에 존재하지 않습니다. [" + expectedHeaderKey + "]")
                        .value(null)
                        .build());
            }
        }

        // 오류 목록 반환.
        return errors;
    }

    /**
     * 헤더 row 범위에서 최대 셀 개수를 찾는다.
     *
     * @param sheet 엑셀 시트
     * @param startRowIndex 시작 row index
     * @param endRowIndex 종료 row index
     * @return 최대 셀 개수
     */
    private static int findMaxCellNum(
            Sheet sheet,
            int startRowIndex,
            int endRowIndex
    ) {

        // 최대 셀 개수.
        int maxCellNum = 0;

        // 헤더 row 범위를 순회한다.
        for (int rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {

            // row를 가져온다.
            Row row = sheet.getRow(rowIndex);

            // row가 없으면 다음 row로 넘어간다.
            if (row == null) {
                continue;
            }

            // getLastCellNum은 short이지만 int로 자동 변환된다.
            maxCellNum = Math.max(maxCellNum, row.getLastCellNum());
        }

        // 최대 셀 개수 반환.
        return maxCellNum;
    }

    /**
     * 헤더명을 비교 가능한 형태로 정규화한다.
     *
     * <p>
     * 현재 기준은 앞뒤 공백 제거만 수행한다.
     * 실무에서 내부 공백 제거까지 허용하면 "수 량" 같은 오타를 통과시킬 수 있으므로
     * 헤더 검증은 엄격하게 가져간다.
     * </p>
     *
     * @param headerName 헤더명
     * @return 정규화된 헤더명
     */
    private static String normalizeHeaderName(String headerName) {

        // null이면 빈 문자열 반환.
        if (headerName == null) {
            return "";
        }

        // 앞뒤 공백만 제거한다.
        return headerName.trim();
    }
}