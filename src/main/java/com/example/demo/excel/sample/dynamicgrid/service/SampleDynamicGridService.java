package com.example.demo.excel.sample.dynamicgrid.service;

import com.example.demo.excel.dto.ExcelDownloadRequest;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridCellSaveDto;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridRow;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSaveRequest;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSearchRequest;
import com.example.demo.excel.sample.dynamicgrid.mapper.SampleDynamicGridMapper;
import com.example.demo.excel.service.ExcelDownloadService;
import com.example.demo.excel.util.ExcelFileNameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SampleDynamicGridService {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String ROW_STATUS_FIELD = "_rowStatus";

    private static final String ROW_STATUS_INSERT = "I";
    private static final String ROW_STATUS_UPDATE = "U";
    private static final String ROW_STATUS_DELETE = "D";
    private static final String ROW_STATUS_NORMAL = "NORMAL";
    private static final String ROW_STATUS_ERROR = "ERROR";

    private static final String FIELD_CATEGORY_NAME = "categoryName";
    private static final String FIELD_APP_NAME = "appName";
    private static final String FIELD_GA_NGA_TYPE = "gaNgaType";
    private static final String FIELD_CUSTOMER_NAME = "customerName";
    private static final String FIELD_SORT3 = "sort3";
    private static final String FIELD_SORT4 = "sort4";
    private static final String FIELD_SORT5 = "sort5";

    private final SampleDynamicGridMapper sampleDynamicGridMapper;
    private final ExcelDownloadService excelDownloadService;

    /**
     * 다이나믹 그리드 데이터를 조회한다.
     *
     * <p>
     * MyBatis는 DB에 저장된 세로형 데이터를 조회하고,
     * 이 서비스에서 AG Grid 표시용 가로형 Map으로 변환한다.
     * </p>
     *
     * @param request 조회조건
     * @return AG Grid rowData
     */
    public List<Map<String, Object>> search(SampleDynamicGridSearchRequest request) {

        // MyBatis로 세로형 데이터를 조회한다.
        List<SampleDynamicGridRow> dbRows = sampleDynamicGridMapper.selectGridRows(request);

        // 세로형 DB row를 가로형 Grid row로 변환한다.
        return toGridRows(dbRows);
    }

    /**
     * 조회조건 기준 전체 데이터를 엑셀로 다운로드한다.
     *
     * <p>
     * 현재 화면에 보이는 10건이 아니라,
     * 조회조건 기준 전체 데이터를 MyBatis로 재조회한 뒤 엑셀로 생성한다.
     * </p>
     *
     * @param request 조회조건 + 엑셀 컬럼 메타
     * @return 엑셀 파일 응답
     */
    public ResponseEntity<byte[]> downloadAll(SampleDynamicGridSearchRequest request) {

        // 조회조건 기준 전체 데이터를 조회한다.
        List<Map<String, Object>> rows = search(request);

        // 공통 ExcelDownloadRequest를 생성한다.
        ExcelDownloadRequest excelRequest = ExcelDownloadRequest.builder()
                .fileName("dynamic-grid-all.xlsx")
                .sheetName("DynamicGrid")
                .columns(request.getColumns())
                .rows(rows)
                .excludeHiddenColumns(request.isExcludeHiddenColumns())
                .useMultiHeader(request.isUseMultiHeader())
                .includeExampleRow(false)
                .build();

        // 공통 ExcelDownloadService로 엑셀 byte를 생성한다.
        byte[] excelBytes = excelDownloadService.createDownloadExcel(excelRequest);

        // 파일 응답을 생성한다.
        return createExcelFileResponse(excelRequest.getFileName(), excelBytes);
    }

    /**
     * MyBatis 조회 결과 세로형 데이터를 AG Grid 가로형 rowData로 변환한다.
     *
     * @param dbRows 세로형 DB rows
     * @return 가로형 Grid rows
     */
    public List<Map<String, Object>> toGridRows(List<SampleDynamicGridRow> dbRows) {

        // row key 기준으로 Grid row를 묶기 위한 Map이다.
        Map<String, Map<String, Object>> rowMap = new LinkedHashMap<>();

        // DB 조회 결과가 없으면 빈 리스트를 반환한다.
        if (dbRows == null || dbRows.isEmpty()) {
            return List.of();
        }

        // 세로형 row를 하나씩 순회한다.
        for (SampleDynamicGridRow dbRow : dbRows) {

            // Grid row를 구분할 key를 생성한다.
            String rowKey = createRowKey(
                    dbRow.getRadioType(),
                    dbRow.getMultiType(),
                    dbRow.getVersionCode(),
                    dbRow.getCategoryName(),
                    dbRow.getAppName(),
                    dbRow.getGaNgaType(),
                    dbRow.getCustomerName()
            );

            // rowKey에 해당하는 Grid row가 없으면 새로 생성한다.
            Map<String, Object> gridRow = rowMap.computeIfAbsent(
                    rowKey,
                    key -> createBaseGridRow(dbRow)
            );

            // 동적 컬럼 field를 생성한다.
            String dynamicField = createDynamicField(
                    dbRow.getMetricType(),
                    dbRow.getYearValue(),
                    dbRow.getQuarterValue()
            );

            // 동적 컬럼 값을 Grid row에 넣는다.
            gridRow.put(dynamicField, dbRow.getValueDecimal());
        }

        // Map value를 List로 변환해서 반환한다.
        return new ArrayList<>(rowMap.values());
    }

    /**
     * Grid 가로형 rowData를 DB 저장용 세로형 cell 목록으로 변환한다.
     *
     * @param request 저장 요청
     * @return 세로형 저장 cell 목록
     */
    public List<SampleDynamicGridCellSaveDto> flattenRows(SampleDynamicGridSaveRequest request) {

        // 결과 cell 목록.
        List<SampleDynamicGridCellSaveDto> cells = new ArrayList<>();

        // 요청 row가 없으면 빈 목록을 반환한다.
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            return cells;
        }

        // rowData를 순회한다.
        for (Map<String, Object> row : request.getRows()) {

            // row가 null이면 건너뛴다.
            if (row == null) {
                continue;
            }

            // row 상태를 읽는다.
            String rowStatus = asString(row.get(ROW_STATUS_FIELD));

            // 저장 대상 row가 아니면 건너뛴다.
            if (!isSaveTargetRow(rowStatus)) {
                continue;
            }

            // 삭제 row는 동적 컬럼 값이 없어도 row key 삭제를 위해 1개 DTO를 생성한다.
            if (ROW_STATUS_DELETE.equals(rowStatus)) {
                cells.add(createDeleteCellDto(request, row, rowStatus));
                continue;
            }

            // 동적 컬럼들을 세로형 cell로 변환한다.
            for (Map.Entry<String, Object> entry : row.entrySet()) {

                // field명을 가져온다.
                String field = entry.getKey();

                // 동적 컬럼 field가 아니면 건너뛴다.
                if (!isDynamicValueField(field)) {
                    continue;
                }

                // field명을 metric/year/quarter로 파싱한다.
                DynamicFieldParts parts = parseDynamicField(field);

                // 값을 BigDecimal로 변환한다.
                BigDecimal value = toBigDecimal(entry.getValue());

                // 값이 null이면 저장하지 않는다.
                if (value == null) {
                    continue;
                }

                // 저장용 DTO를 생성한다.
                SampleDynamicGridCellSaveDto cell = SampleDynamicGridCellSaveDto.builder()
                        .rowStatus(rowStatus)
                        .baseYm(createBaseYm(parts.yearValue(), parts.quarterValue()))
                        .radioType(request.getRadioType())
                        .multiType(resolveMultiType(request))
                        .versionCode(request.getSingleSelectValue())
                        .categoryName(asString(row.get(FIELD_CATEGORY_NAME)))
                        .appName(asString(row.get(FIELD_APP_NAME)))
                        .gaNgaType(asString(row.get(FIELD_GA_NGA_TYPE)))
                        .customerName(asString(row.get(FIELD_CUSTOMER_NAME)))
                        .sort3(toInteger(row.get(FIELD_SORT3)))
                        .sort4(toInteger(row.get(FIELD_SORT4)))
                        .sort5(toInteger(row.get(FIELD_SORT5)))
                        .metricType(parts.metricType())
                        .yearValue(parts.yearValue())
                        .quarterValue(parts.quarterValue())
                        .valueDecimal(value)
                        .build();

                // 결과 목록에 추가한다.
                cells.add(cell);
            }
        }

        // 세로형 저장 cell 목록을 반환한다.
        return cells;
    }

    /**
     * DB row의 기본 컬럼을 Grid row로 생성한다.
     *
     * @param dbRow DB row
     * @return Grid row
     */
    private Map<String, Object> createBaseGridRow(SampleDynamicGridRow dbRow) {

        // 순서 보장을 위해 LinkedHashMap을 사용한다.
        Map<String, Object> row = new LinkedHashMap<>();

        // 조회된 row는 기본적으로 NORMAL 상태다.
        row.put(ROW_STATUS_FIELD, ROW_STATUS_NORMAL);

        // 기본 컬럼들을 세팅한다.
        row.put(FIELD_CATEGORY_NAME, dbRow.getCategoryName());
        row.put(FIELD_APP_NAME, dbRow.getAppName());
        row.put(FIELD_GA_NGA_TYPE, dbRow.getGaNgaType());
        row.put(FIELD_CUSTOMER_NAME, dbRow.getCustomerName());

        // 스타일/계층 판단용 sort 필드를 세팅한다.
        row.put(FIELD_SORT3, dbRow.getSort3());
        row.put(FIELD_SORT4, dbRow.getSort4());
        row.put(FIELD_SORT5, dbRow.getSort5());

        return row;
    }

    /**
     * row key를 생성한다.
     */
    private String createRowKey(
            String radioType,
            String multiType,
            String versionCode,
            String categoryName,
            String appName,
            String gaNgaType,
            String customerName
    ) {
        return String.join("|",
                nullToEmpty(radioType),
                nullToEmpty(multiType),
                nullToEmpty(versionCode),
                nullToEmpty(categoryName),
                nullToEmpty(appName),
                nullToEmpty(gaNgaType),
                nullToEmpty(customerName)
        );
    }

    /**
     * 동적 컬럼 field를 생성한다.
     *
     * <p>
     * 규칙:
     * QTY + 2026 + 01  -> QTY_Q202601
     * QTY + 2026 + TOT -> QTY_Q2026_TOT
     * </p>
     */
    private String createDynamicField(
            String metricType,
            String yearValue,
            String quarterValue
    ) {

        // TOTAL 컬럼이면 _TOT suffix를 붙인다.
        if ("TOT".equalsIgnoreCase(quarterValue)) {
            return metricType + "_Q" + yearValue + "_TOT";
        }

        // 일반 월/분기 컬럼.
        return metricType + "_Q" + yearValue + quarterValue;
    }

    /**
     * 동적 컬럼 field 여부를 판단한다.
     */
    private boolean isDynamicValueField(String field) {

        // field가 없으면 false.
        if (!StringUtils.hasText(field)) {
            return false;
        }

        // 시스템/기본 컬럼은 제외한다.
        if (field.startsWith("_")) {
            return false;
        }

        if (FIELD_CATEGORY_NAME.equals(field)
                || FIELD_APP_NAME.equals(field)
                || FIELD_GA_NGA_TYPE.equals(field)
                || FIELD_CUSTOMER_NAME.equals(field)
                || FIELD_SORT3.equals(field)
                || FIELD_SORT4.equals(field)
                || FIELD_SORT5.equals(field)) {
            return false;
        }

        // 동적 컬럼은 _Q를 포함하는 규칙으로 판단한다.
        return field.contains("_Q");
    }

    /**
     * 동적 컬럼 field를 metric/year/quarter로 파싱한다.
     *
     * <p>
     * 예:
     * QTY_Q202601 -> metricType=QTY, yearValue=2026, quarterValue=01
     * QTY_Q2026_TOT -> metricType=QTY, yearValue=2026, quarterValue=TOT
     * </p>
     */
    private DynamicFieldParts parseDynamicField(String field) {

        // _Q 기준으로 metric과 나머지 값을 분리한다.
        String[] split = field.split("_Q");

        if (split.length != 2) {
            throw new IllegalArgumentException("동적 컬럼 형식이 올바르지 않습니다. field=" + field);
        }

        // metricType.
        String metricType = split[0];

        // Q 뒤의 년월 또는 년도_TOT.
        String yearQuarterPart = split[1];

        // TOTAL 컬럼이면 2026_TOT 형식이다.
        if (yearQuarterPart.endsWith("_TOT")) {
            String yearValue = yearQuarterPart.substring(0, 4);
            return new DynamicFieldParts(metricType, yearValue, "TOT");
        }

        // 일반 컬럼은 202601 형식이다.
        if (yearQuarterPart.length() < 6) {
            throw new IllegalArgumentException("동적 컬럼 년월 형식이 올바르지 않습니다. field=" + field);
        }

        String yearValue = yearQuarterPart.substring(0, 4);
        String quarterValue = yearQuarterPart.substring(4);

        return new DynamicFieldParts(metricType, yearValue, quarterValue);
    }

    /**
     * 기준년월을 생성한다.
     */
    private String createBaseYm(String yearValue, String quarterValue) {

        // TOTAL 컬럼은 12월 기준으로 저장한다.
        if ("TOT".equalsIgnoreCase(quarterValue)) {
            return yearValue + "12";
        }

        return yearValue + quarterValue;
    }

    /**
     * 저장 대상 row 여부를 판단한다.
     */
    private boolean isSaveTargetRow(String rowStatus) {
        return ROW_STATUS_INSERT.equals(rowStatus)
                || ROW_STATUS_UPDATE.equals(rowStatus)
                || ROW_STATUS_DELETE.equals(rowStatus);
    }

    /**
     * 삭제용 DTO를 생성한다.
     */
    private SampleDynamicGridCellSaveDto createDeleteCellDto(
            SampleDynamicGridSaveRequest request,
            Map<String, Object> row,
            String rowStatus
    ) {
        return SampleDynamicGridCellSaveDto.builder()
                .rowStatus(rowStatus)
                .radioType(request.getRadioType())
                .multiType(resolveMultiType(request))
                .versionCode(request.getSingleSelectValue())
                .categoryName(asString(row.get(FIELD_CATEGORY_NAME)))
                .appName(asString(row.get(FIELD_APP_NAME)))
                .gaNgaType(asString(row.get(FIELD_GA_NGA_TYPE)))
                .customerName(asString(row.get(FIELD_CUSTOMER_NAME)))
                .sort3(toInteger(row.get(FIELD_SORT3)))
                .sort4(toInteger(row.get(FIELD_SORT4)))
                .sort5(toInteger(row.get(FIELD_SORT5)))
                .build();
    }

    /**
     * 다중 선택값을 저장용 단일 문자열로 결정한다.
     *
     * <p>
     * 현재 샘플은 첫 번째 multiSelect 값을 대표값으로 사용한다.
     * 실무에서는 row별 multiType이 따로 있으면 row의 값을 우선 사용하거나,
     * 선택 조건별로 row를 분리 저장하면 된다.
     * </p>
     */
    private String resolveMultiType(SampleDynamicGridSaveRequest request) {

        if (request.getMultiSelectValues() == null || request.getMultiSelectValues().isEmpty()) {
            return null;
        }

        return request.getMultiSelectValues().get(0);
    }

    /**
     * 파일 다운로드 응답을 생성한다.
     */
    private ResponseEntity<byte[]> createExcelFileResponse(
            String fileName,
            byte[] excelBytes
    ) {

        String contentDisposition = ExcelFileNameUtils.buildContentDisposition(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .body(excelBytes);
    }

    private String asString(Object value) {

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Integer toInteger(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        String stringValue = String.valueOf(value);

        if (!StringUtils.hasText(stringValue)) {
            return null;
        }

        return Integer.valueOf(stringValue);
    }

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal bigDecimalValue) {
            return bigDecimalValue;
        }

        if (value instanceof Number numberValue) {
            return new BigDecimal(numberValue.toString());
        }

        String stringValue = String.valueOf(value);

        if (!StringUtils.hasText(stringValue)) {
            return null;
        }

        return new BigDecimal(stringValue.replace(",", ""));
    }

    /**
     * 동적 컬럼 파싱 결과.
     */
    private record DynamicFieldParts(
            String metricType,
            String yearValue,
            String quarterValue
    ) {
    }
}