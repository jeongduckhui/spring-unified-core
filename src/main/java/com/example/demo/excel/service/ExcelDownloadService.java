package com.example.demo.excel.service;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelDownloadRequest;
import com.example.demo.excel.dto.ExcelErrorRowsDownloadRequest;
import com.example.demo.excel.dto.ExcelTemplateDownloadRequest;
import com.example.demo.excel.util.ExcelColumnUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 엑셀 다운로드 서비스.
 *
 * <p>
 * Apache POI SXSSFWorkbook을 사용해 엑셀 파일을 생성한다.
 * </p>
 *
 * <p>
 * 지원 기능:
 * 1. 일반 다운로드
 * 2. 템플릿 다운로드
 * 3. 오류 행 다운로드
 * 4. 동적 컬럼
 * 5. 다중 헤더
 * 6. 필수 컬럼 표시
 * 7. 예제 데이터 행
 * </p>
 */
@Slf4j
@Service
public class ExcelDownloadService {

    /**
     * 업로드 결과 row 상태 필드명.
     */
    private static final String ROW_STATUS_FIELD = "_rowStatus";

    /**
     * 업로드 결과 row 오류 메시지 필드명.
     */
    private static final String ROW_ERROR_MESSAGES_FIELD = "_errorMessages";

    /**
     * 오류 row 상태값.
     */
    private static final String ROW_STATUS_ERROR = "ERROR";

    /**
     * 기본 시트명.
     */
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    /**
     * SXSSFWorkbook 메모리 보관 row 수.
     *
     * <p>
     * 이 개수를 초과하면 임시 파일로 flush된다.
     * 대용량 다운로드를 고려해 SXSSF를 사용한다.
     * </p>
     */
    private static final int ROW_ACCESS_WINDOW_SIZE = 100;

    /**
     * 일반 엑셀 다운로드 파일을 생성한다.
     *
     * @param request 엑셀 다운로드 요청
     * @return 엑셀 파일 byte 배열
     */
    public byte[] createDownloadExcel(ExcelDownloadRequest request) {

        // 요청 컬럼을 다운로드 가능한 컬럼 목록으로 정리한다.
        List<ExcelColumnMeta> columns = ExcelColumnUtils.normalizeColumns(
                request.getColumns(),
                request.isExcludeHiddenColumns()
        );

        // 데이터 목록을 가져온다.
        List<Map<String, Object>> rows = request.getRows();

        // 엑셀 파일을 생성한다.
        return createExcelBytes(
                request.getSheetName(),
                columns,
                rows,
                request.isUseMultiHeader(),
                request.isIncludeExampleRow()
        );
    }

    /**
     * 엑셀 템플릿 다운로드 파일을 생성한다.
     *
     * @param request 엑셀 템플릿 다운로드 요청
     * @return 엑셀 파일 byte 배열
     */
    public byte[] createTemplateExcel(ExcelTemplateDownloadRequest request) {

        // 요청 컬럼을 다운로드 가능한 컬럼 목록으로 정리한다.
        List<ExcelColumnMeta> columns = ExcelColumnUtils.normalizeColumns(
                request.getColumns(),
                request.isExcludeHiddenColumns()
        );

        // 템플릿은 실제 데이터가 없으므로 빈 리스트를 사용한다.
        List<Map<String, Object>> rows = List.of();

        log.info(
                "useMultiHeader={}, hasHeaderPath={}",
                request.isUseMultiHeader(),
                hasHeaderPath(columns)
        );

//        log.info("headerPath={}",
//                columns.get(0).getHeaderPath());

        // 엑셀 파일을 생성한다.
        return createExcelBytes(
                request.getSheetName(),
                columns,
                rows,
                request.isUseMultiHeader(),
                request.isIncludeExampleRow()
        );
    }

    /**
     * 오류 행 엑셀 다운로드 파일을 생성한다.
     *
     * <p>
     * 업로드 결과 rows 중 _rowStatus = ERROR 인 행만 필터링해서 엑셀 파일을 생성한다.
     * </p>
     *
     * @param request 오류 행 다운로드 요청
     * @return 엑셀 파일 byte 배열
     */
    public byte[] createErrorRowsExcel(ExcelErrorRowsDownloadRequest request) {

        // 요청 컬럼을 다운로드 가능한 컬럼 목록으로 정리한다.
        List<ExcelColumnMeta> columns = ExcelColumnUtils.normalizeColumns(
                request.getColumns(),
                request.isExcludeHiddenColumns()
        );

        // 오류 메시지 컬럼 포함 옵션이 켜져 있으면 마지막에 오류내용 컬럼을 추가한다.
        List<ExcelColumnMeta> errorDownloadColumns = appendErrorMessageColumnIfNeeded(
                columns,
                request.isIncludeErrorMessageColumn()
        );

        // 업로드 결과 rows 중 오류 행만 필터링한다.
        List<Map<String, Object>> errorRows = filterErrorRows(request.getRows());

        // 오류 행 엑셀 파일을 생성한다.
        return createExcelBytes(
                request.getSheetName(),
                errorDownloadColumns,
                errorRows,
                request.isUseMultiHeader(),
                false
        );
    }

    /**
     * 오류 메시지 컬럼을 필요한 경우 추가한다.
     *
     * @param columns 기존 컬럼 목록
     * @param includeErrorMessageColumn 오류 메시지 컬럼 포함 여부
     * @return 오류 메시지 컬럼이 반영된 컬럼 목록
     */
    private List<ExcelColumnMeta> appendErrorMessageColumnIfNeeded(
            List<ExcelColumnMeta> columns,
            boolean includeErrorMessageColumn
    ) {

        // 새로운 컬럼 목록을 생성한다.
        List<ExcelColumnMeta> result = new ArrayList<>(columns);

        // 오류 메시지 컬럼 포함 옵션이 꺼져 있으면 기존 컬럼 목록을 그대로 반환한다.
        if (!includeErrorMessageColumn) {
            return result;
        }

        // 오류 메시지 컬럼을 추가한다.
        result.add(ExcelColumnMeta.builder()
                .field(ROW_ERROR_MESSAGES_FIELD)
                .headerName("오류내용")
                .required(false)
                .hidden(false)
                .order(resolveNextOrder(columns))
                .build());

        // 오류 메시지 컬럼이 포함된 목록을 반환한다.
        return result;
    }

    /**
     * 다음 컬럼 order를 계산한다.
     *
     * @param columns 컬럼 목록
     * @return 다음 order
     */
    private Integer resolveNextOrder(List<ExcelColumnMeta> columns) {

        // 컬럼이 없으면 1을 반환한다.
        if (columns == null || columns.isEmpty()) {
            return 1;
        }

        // 가장 큰 order를 찾아 +1 한다.
        return columns.stream()
                .map(ExcelColumnMeta::getOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .orElse(columns.size()) + 1;
    }

    /**
     * 업로드 결과 rows 중 오류 행만 필터링한다.
     *
     * @param rows 업로드 결과 rows
     * @return 오류 행 목록
     */
    private List<Map<String, Object>> filterErrorRows(List<Map<String, Object>> rows) {

        // rows가 없으면 빈 리스트를 반환한다.
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        // 오류 행만 담을 목록을 생성한다.
        List<Map<String, Object>> errorRows = new ArrayList<>();

        // rows를 순회한다.
        for (Map<String, Object> row : rows) {

            // row가 null이면 건너뛴다.
            if (row == null) {
                continue;
            }

            // row 상태값을 가져온다.
            Object rowStatus = row.get(ROW_STATUS_FIELD);

            // ERROR 상태가 아니면 건너뛴다.
            if (!ROW_STATUS_ERROR.equals(rowStatus)) {
                continue;
            }

            // 원본 row를 직접 수정하지 않도록 복사한다.
            Map<String, Object> copiedRow = new LinkedHashMap<>(row);

            // 오류 행 목록에 추가한다.
            errorRows.add(copiedRow);
        }

        // 오류 행 목록을 반환한다.
        return errorRows;
    }

    /**
     * 엑셀 byte 배열을 생성한다.
     *
     * @param sheetName 시트명
     * @param columns 컬럼 메타 목록
     * @param rows 데이터 목록
     * @param useMultiHeader 다중 헤더 사용 여부
     * @param includeExampleRow 예제 행 포함 여부
     * @return 엑셀 파일 byte 배열
     */
    private byte[] createExcelBytes(
            String sheetName,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> rows,
            boolean useMultiHeader,
            boolean includeExampleRow
    ) {

        // SXSSFWorkbook은 대용량 엑셀 생성을 위한 Workbook이다.
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 임시 파일 압축을 활성화한다.
            workbook.setCompressTempFiles(true);

            // 시트를 생성한다.
            var sheet = workbook.createSheet(resolveSheetName(sheetName));

            // 헤더 스타일을 생성한다.
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 필수 컬럼 헤더 스타일을 생성한다.
            CellStyle requiredHeaderStyle = createRequiredHeaderStyle(workbook);

            // 일반 데이터 셀 스타일을 생성한다.
            CellStyle bodyStyle = createBodyStyle(workbook);

            // 현재 작성 중인 row index.
            int rowIndex;

            // 다중 헤더 사용 여부에 따라 헤더 생성 방식을 분기한다.
            if (useMultiHeader) {

                // headerPath 기반 다중 헤더가 존재하는지 확인한다.
                boolean useHeaderPath = hasHeaderPath(columns);

                // headerPath가 존재하면 2단/3단/그 이상 헤더 생성 로직을 사용한다.
                if (useHeaderPath) {

                    rowIndex = createHeaderPathRows(
                            sheet,
                            columns,
                            headerStyle,
                            requiredHeaderStyle
                    );

                }

                // 기존 parentHeader 기반 2단 헤더를 사용한다.
                else {

                    rowIndex = createMultiHeaderRows(
                            sheet,
                            columns,
                            headerStyle,
                            requiredHeaderStyle
                    );
                }

            } else {

                rowIndex = createSingleHeaderRow(
                        sheet,
                        columns,
                        headerStyle,
                        requiredHeaderStyle
                );
            }

            // 템플릿 예제 행을 생성한다.
            if (includeExampleRow) {
                createExampleRow(sheet, rowIndex, columns, bodyStyle);
                rowIndex++;
            }

            // 실제 데이터 행을 생성한다.
            createBodyRows(sheet, rowIndex, columns, rows, bodyStyle);

            // 컬럼 너비를 설정한다.
            applyColumnWidth(sheet, columns);

            // Workbook을 byte 배열로 변환한다.
            workbook.write(outputStream);

            // SXSSFWorkbook이 사용하는 임시 파일을 제거한다.
            workbook.dispose();

            // byte 배열을 반환한다.
            return outputStream.toByteArray();

        } catch (IOException e) {

            // 엑셀 생성 중 오류가 발생하면 런타임 예외로 전환한다.
            log.error("엑셀 파일 생성 중 오류가 발생했습니다.", e);
            throw new IllegalStateException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * headerPath 기반 다중 헤더 사용 여부를 판단한다.
     *
     * <p>
     * ExcelColumnMeta.headerPath가 존재하고
     * 실제 헤더 depth가 2개 이상이면
     * headerPath 기반 다중 헤더를 사용한다.
     * </p>
     *
     * @param columns 컬럼 목록
     * @return headerPath 사용 여부
     */
    private boolean hasHeaderPath(List<ExcelColumnMeta> columns) {

        // 컬럼이 없으면 false를 반환한다.
        if (columns == null || columns.isEmpty()) {
            return false;
        }

        // 컬럼 목록을 순회한다.
        for (ExcelColumnMeta column : columns) {

            log.info(
                    "field={}, headerPath={}",
                    column.getField(),
                    column.getHeaderPath()
            );

            log.info(
                    "field={}, headerPath={}",
                    column.getField(),
                    column.getHeaderPath()
            );

            // 컬럼이 없으면 다음 컬럼으로 넘어간다.
            if (column == null) {
                continue;
            }

            // headerPath가 없으면 다음 컬럼으로 넘어간다.
            if (column.getHeaderPath() == null) {
                continue;
            }

            // 실제 헤더 depth를 계산한다.
            long depthCount = column.getHeaderPath()
                    .stream()
                    .filter(StringUtils::hasText)
                    .count();

            // depth가 2 이상이면 headerPath 기반 헤더로 판단한다.
            if (depthCount > 1) {
                return true;
            }
        }

        // headerPath 기반 헤더가 없으면 false를 반환한다.
        return false;
    }

    /**
     * 단일 헤더 row를 생성한다.
     *
     * @param sheet 시트
     * @param columns 컬럼 메타 목록
     * @param headerStyle 일반 헤더 스타일
     * @param requiredHeaderStyle 필수 헤더 스타일
     * @return 다음 row index
     */
    private int createSingleHeaderRow(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<ExcelColumnMeta> columns,
            CellStyle headerStyle,
            CellStyle requiredHeaderStyle
    ) {

        // 첫 번째 행에 헤더를 생성한다.
        Row headerRow = sheet.createRow(0);

        // 컬럼 개수만큼 셀을 생성한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            // 컬럼 메타를 가져온다.
            ExcelColumnMeta column = columns.get(columnIndex);

            // 헤더 셀을 생성한다.
            Cell cell = headerRow.createCell(columnIndex);

            // 헤더명을 세팅한다.
            cell.setCellValue(column.getHeaderName());

            // 필수 컬럼이면 필수 스타일, 아니면 일반 헤더 스타일을 적용한다.
            cell.setCellStyle(column.isRequired() ? requiredHeaderStyle : headerStyle);
        }

        // 다음 데이터 시작 row index를 반환한다.
        return 1;
    }

    /**
     * 다중 헤더 row를 생성한다.
     *
     * <p>
     * 1차 구현 기준:
     * parentHeader가 있으면 2단 헤더로 생성한다.
     * parentHeader가 없으면 상위/하위 헤더를 같은 이름으로 병합 처리한다.
     * </p>
     *
     * @param sheet 시트
     * @param columns 컬럼 메타 목록
     * @param headerStyle 일반 헤더 스타일
     * @param requiredHeaderStyle 필수 헤더 스타일
     * @return 다음 row index
     */
    private int createMultiHeaderRows(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<ExcelColumnMeta> columns,
            CellStyle headerStyle,
            CellStyle requiredHeaderStyle
    ) {

        // 상위 헤더 행을 생성한다.
        Row parentRow = sheet.createRow(0);

        // 하위 헤더 행을 생성한다.
        Row childRow = sheet.createRow(1);

        // 컬럼을 순회하면서 헤더 셀을 생성한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            // 컬럼 메타를 가져온다.
            ExcelColumnMeta column = columns.get(columnIndex);

            // 상위 헤더명은 parentHeader가 있으면 parentHeader, 없으면 headerName을 사용한다.
            String parentHeaderName = StringUtils.hasText(column.getParentHeader())
                    ? column.getParentHeader()
                    : column.getHeaderName();

            // 하위 헤더명은 headerName을 사용한다.
            String childHeaderName = column.getHeaderName();

            // 상위 헤더 셀을 생성한다.
            Cell parentCell = parentRow.createCell(columnIndex);

            // 상위 헤더명을 세팅한다.
            parentCell.setCellValue(parentHeaderName);

            // 상위 헤더 스타일을 적용한다.
            parentCell.setCellStyle(headerStyle);

            // 하위 헤더 셀을 생성한다.
            Cell childCell = childRow.createCell(columnIndex);

            // 하위 헤더명을 세팅한다.
            childCell.setCellValue(childHeaderName);

            // 필수 컬럼이면 필수 스타일, 아니면 일반 헤더 스타일을 적용한다.
            childCell.setCellStyle(column.isRequired() ? requiredHeaderStyle : headerStyle);

            // parentHeader가 없는 컬럼은 세로 병합한다.
            if (!StringUtils.hasText(column.getParentHeader())) {
                sheet.addMergedRegion(new CellRangeAddress(0, 1, columnIndex, columnIndex));
            }
        }

        // 같은 parentHeader가 연속된 컬럼은 가로 병합한다.
        mergeSameParentHeaders(sheet, columns);

        // 다음 데이터 시작 row index를 반환한다.
        return 2;
    }

    /**
     * headerPath 기반 다중 헤더 row를 생성한다.
     *
     * <p>
     * 2단/3단/그 이상 헤더를 동일한 방식으로 처리한다.
     * ExcelColumnMeta.headerPath가 있으면 그것을 우선 사용한다.
     * headerPath가 없으면 기존 parentHeader/headerName 구조로 fallback 한다.
     * </p>
     *
     * @param sheet 시트
     * @param columns 컬럼 메타 목록
     * @param headerStyle 일반 헤더 스타일
     * @param requiredHeaderStyle 필수 헤더 스타일
     * @return 데이터 시작 row index
     */
    private int createHeaderPathRows(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<ExcelColumnMeta> columns,
            CellStyle headerStyle,
            CellStyle requiredHeaderStyle
    ) {

        // 컬럼이 없으면 첫 번째 row부터 데이터 시작.
        if (columns == null || columns.isEmpty()) {
            return 0;
        }

        // 각 컬럼별 헤더 경로를 생성한다.
        List<List<String>> headerPaths = columns.stream()
                .map(this::resolveHeaderPath)
                .toList();

        // 최대 헤더 깊이를 구한다.
        int maxDepth = headerPaths.stream()
                .mapToInt(List::size)
                .max()
                .orElse(1);

        // 헤더 row를 생성한다.
        List<Row> headerRows = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < maxDepth; rowIndex++) {
            headerRows.add(sheet.createRow(rowIndex));
        }

        // 헤더 셀을 생성한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            ExcelColumnMeta column = columns.get(columnIndex);
            List<String> headerPath = headerPaths.get(columnIndex);

            for (int depthIndex = 0; depthIndex < maxDepth; depthIndex++) {

                Row row = headerRows.get(depthIndex);
                Cell cell = row.createCell(columnIndex);

                String headerText = resolveHeaderTextAtDepth(headerPath, depthIndex, maxDepth);

                cell.setCellValue(headerText);

                boolean isLastDepth = depthIndex == maxDepth - 1;

                cell.setCellStyle(
                        isLastDepth && column.isRequired()
                                ? requiredHeaderStyle
                                : headerStyle
                );
            }
        }

        // 동일 헤더 그룹을 가로 병합한다.
        mergeHorizontalHeaderGroups(sheet, headerPaths, maxDepth);

        // 짧은 headerPath는 세로 병합한다.
        mergeVerticalHeaderGroups(sheet, headerPaths, maxDepth);

        // 데이터 시작 row index는 헤더 깊이와 같다.
        return maxDepth;
    }

    /**
     * 컬럼의 헤더 경로를 결정한다.
     *
     * <p>
     * 우선순위:
     * 1. headerPath
     * 2. parentHeader + headerName
     * 3. headerName
     * </p>
     *
     * @param column 컬럼 메타
     * @return 헤더 경로
     */
    private List<String> resolveHeaderPath(ExcelColumnMeta column) {

        // headerPath가 있으면 공백 값을 제거하고 사용한다.
        if (column.getHeaderPath() != null && !column.getHeaderPath().isEmpty()) {
            List<String> headerPath = column.getHeaderPath().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList();

            if (!headerPath.isEmpty()) {
                return headerPath;
            }
        }

        // 기존 2단 헤더 호환: parentHeader + headerName.
        if (StringUtils.hasText(column.getParentHeader())) {
            return List.of(
                    column.getParentHeader().trim(),
                    nullToEmpty(column.getHeaderName())
            );
        }

        // 단일 헤더 fallback.
        return List.of(nullToEmpty(column.getHeaderName()));
    }

    /**
     * 특정 depth의 헤더 텍스트를 반환한다.
     *
     * <p>
     * headerPath 깊이가 maxDepth보다 짧으면 마지막 헤더명을 계속 반환한다.
     * 이후 세로 병합으로 하나의 셀처럼 보이게 처리한다.
     * </p>
     *
     * @param headerPath 헤더 경로
     * @param depthIndex depth index
     * @param maxDepth 최대 헤더 깊이
     * @return 헤더 텍스트
     */
    private String resolveHeaderTextAtDepth(
            List<String> headerPath,
            int depthIndex,
            int maxDepth
    ) {

        if (headerPath == null || headerPath.isEmpty()) {
            return "";
        }

        if (depthIndex < headerPath.size()) {
            return headerPath.get(depthIndex);
        }

        return headerPath.get(headerPath.size() - 1);
    }

    /**
     * 동일한 헤더 그룹을 가로 병합한다.
     *
     * <p>
     * 같은 depth에서 같은 텍스트가 연속되어 있고,
     * 상위 path도 동일한 경우 병합한다.
     * </p>
     *
     * @param sheet 시트
     * @param headerPaths 컬럼별 헤더 경로
     * @param maxDepth 최대 헤더 깊이
     */
    private void mergeHorizontalHeaderGroups(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<List<String>> headerPaths,
            int maxDepth
    ) {

        for (int depthIndex = 0; depthIndex < maxDepth; depthIndex++) {

            int startColumnIndex = 0;

            while (startColumnIndex < headerPaths.size()) {

                int endColumnIndex = startColumnIndex;

                while (endColumnIndex + 1 < headerPaths.size()
                        && isSameHeaderGroup(headerPaths, startColumnIndex, endColumnIndex + 1, depthIndex, maxDepth)) {
                    endColumnIndex++;
                }

                if (endColumnIndex > startColumnIndex) {
                    sheet.addMergedRegion(new CellRangeAddress(
                            depthIndex,
                            depthIndex,
                            startColumnIndex,
                            endColumnIndex
                    ));
                }

                startColumnIndex = endColumnIndex + 1;
            }
        }
    }

    /**
     * 짧은 headerPath를 세로 병합한다.
     *
     * <p>
     * 예:
     * 기본 컬럼 "구분"은 headerPath가 ["구분"]이고,
     * 동적 컬럼은 ["QTY", "2026", "1M"]이면,
     * "구분" 컬럼은 0~2 row를 세로 병합한다.
     * </p>
     *
     * @param sheet 시트
     * @param headerPaths 컬럼별 헤더 경로
     * @param maxDepth 최대 헤더 깊이
     */
    private void mergeVerticalHeaderGroups(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<List<String>> headerPaths,
            int maxDepth
    ) {

        for (int columnIndex = 0; columnIndex < headerPaths.size(); columnIndex++) {

            List<String> headerPath = headerPaths.get(columnIndex);

            if (headerPath == null || headerPath.isEmpty()) {
                continue;
            }

            int pathDepth = headerPath.size();

            if (pathDepth >= maxDepth) {
                continue;
            }

            sheet.addMergedRegion(new CellRangeAddress(
                    pathDepth - 1,
                    maxDepth - 1,
                    columnIndex,
                    columnIndex
            ));
        }
    }

    /**
     * null 문자열을 빈 문자열로 변환한다.
     *
     * @param value 문자열
     * @return null이면 빈 문자열
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 두 컬럼이 특정 depth에서 같은 헤더 그룹인지 판단한다.
     *
     * @param headerPaths 컬럼별 헤더 경로
     * @param leftIndex 왼쪽 컬럼 index
     * @param rightIndex 오른쪽 컬럼 index
     * @param depthIndex depth index
     * @param maxDepth 최대 헤더 깊이
     * @return 같은 그룹이면 true
     */
    private boolean isSameHeaderGroup(
            List<List<String>> headerPaths,
            int leftIndex,
            int rightIndex,
            int depthIndex,
            int maxDepth
    ) {

        List<String> leftPath = headerPaths.get(leftIndex);
        List<String> rightPath = headerPaths.get(rightIndex);

        // 현재 depth까지의 path가 모두 같아야 같은 그룹이다.
        for (int index = 0; index <= depthIndex; index++) {
            String leftText = resolveHeaderTextAtDepth(leftPath, index, maxDepth);
            String rightText = resolveHeaderTextAtDepth(rightPath, index, maxDepth);

            if (!Objects.equals(leftText, rightText)) {
                return false;
            }
        }

        // leaf depth에서는 가로 병합하지 않는다.
        return depthIndex < maxDepth - 1;
    }

    /**
     * 연속된 동일 상위 헤더를 병합한다.
     *
     * @param sheet 시트
     * @param columns 컬럼 메타 목록
     */
    private void mergeSameParentHeaders(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<ExcelColumnMeta> columns
    ) {

        // 병합 시작 인덱스.
        int startIndex = -1;

        // 현재 상위 헤더명.
        String currentParentHeader = null;

        // 컬럼 목록을 순회한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            // 컬럼 메타를 가져온다.
            ExcelColumnMeta column = columns.get(columnIndex);

            // parentHeader가 없는 컬럼은 가로 병합 대상이 아니다.
            if (!StringUtils.hasText(column.getParentHeader())) {

                // 기존 병합 대상이 있으면 마무리한다.
                mergeParentHeaderIfNeeded(sheet, startIndex, columnIndex - 1);

                // 병합 상태를 초기화한다.
                startIndex = -1;
                currentParentHeader = null;
                continue;
            }

            // 처음 만난 parentHeader이면 병합 시작점을 잡는다.
            if (currentParentHeader == null) {
                currentParentHeader = column.getParentHeader();
                startIndex = columnIndex;
                continue;
            }

            // 이전 parentHeader와 다르면 이전 그룹을 병합하고 새 그룹을 시작한다.
            if (!currentParentHeader.equals(column.getParentHeader())) {
                mergeParentHeaderIfNeeded(sheet, startIndex, columnIndex - 1);
                currentParentHeader = column.getParentHeader();
                startIndex = columnIndex;
            }
        }

        // 마지막 그룹을 병합한다.
        mergeParentHeaderIfNeeded(sheet, startIndex, columns.size() - 1);
    }

    /**
     * 상위 헤더 병합이 필요한 경우 병합한다.
     *
     * @param sheet 시트
     * @param startIndex 시작 컬럼 인덱스
     * @param endIndex 종료 컬럼 인덱스
     */
    private void mergeParentHeaderIfNeeded(
            org.apache.poi.ss.usermodel.Sheet sheet,
            int startIndex,
            int endIndex
    ) {

        // 시작 인덱스가 유효하지 않으면 병합하지 않는다.
        if (startIndex < 0) {
            return;
        }

        // 컬럼이 2개 이상일 때만 가로 병합한다.
        if (endIndex > startIndex) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, startIndex, endIndex));
        }
    }

    /**
     * 예제 데이터 row를 생성한다.
     *
     * @param sheet 시트
     * @param rowIndex row index
     * @param columns 컬럼 메타 목록
     * @param bodyStyle 바디 스타일
     */
    private void createExampleRow(
            org.apache.poi.ss.usermodel.Sheet sheet,
            int rowIndex,
            List<ExcelColumnMeta> columns,
            CellStyle bodyStyle
    ) {

        // 예제 행을 생성한다.
        Row row = sheet.createRow(rowIndex);

        // 컬럼 개수만큼 셀을 생성한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            // 컬럼 메타를 가져온다.
            ExcelColumnMeta column = columns.get(columnIndex);

            // 셀을 생성한다.
            Cell cell = row.createCell(columnIndex);

            // 예제 값을 세팅한다.
            cell.setCellValue(column.getExampleValue());

            // 바디 스타일을 적용한다.
            cell.setCellStyle(bodyStyle);
        }
    }

    /**
     * 데이터 row 목록을 생성한다.
     *
     * @param sheet 시트
     * @param startRowIndex 데이터 시작 row index
     * @param columns 컬럼 메타 목록
     * @param rows 데이터 목록
     * @param bodyStyle 바디 스타일
     */
    private void createBodyRows(
            org.apache.poi.ss.usermodel.Sheet sheet,
            int startRowIndex,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> rows,
            CellStyle bodyStyle
    ) {

        // 데이터가 없으면 처리하지 않는다.
        if (rows == null || rows.isEmpty()) {
            return;
        }

        // 데이터 행을 순회한다.
        for (int rowOffset = 0; rowOffset < rows.size(); rowOffset++) {

            // 현재 데이터 Map을 가져온다.
            Map<String, Object> rowData = rows.get(rowOffset);

            // 엑셀 Row를 생성한다.
            Row row = sheet.createRow(startRowIndex + rowOffset);

            // 컬럼 기준으로 셀을 생성한다.
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

                // 컬럼 메타를 가져온다.
                ExcelColumnMeta column = columns.get(columnIndex);

                // 필드명으로 값을 가져온다.
                Object value = rowData.get(column.getField());

                // 셀을 생성한다.
                Cell cell = row.createCell(columnIndex);

                // 셀 값을 세팅한다.
                setCellValue(cell, value);

                // 바디 스타일을 적용한다.
                cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * 셀 값을 타입에 맞게 세팅한다.
     *
     * @param cell 셀
     * @param value 값
     */
    private void setCellValue(Cell cell, Object value) {

        // 값이 null이면 빈 문자열로 처리한다.
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        // 문자열이면 문자열로 세팅한다.
        if (value instanceof String stringValue) {
            cell.setCellValue(stringValue);
            return;
        }

        // Integer이면 숫자로 세팅한다.
        if (value instanceof Integer integerValue) {
            cell.setCellValue(integerValue);
            return;
        }

        // Long이면 숫자로 세팅한다.
        if (value instanceof Long longValue) {
            cell.setCellValue(longValue);
            return;
        }

        // Double이면 숫자로 세팅한다.
        if (value instanceof Double doubleValue) {
            cell.setCellValue(doubleValue);
            return;
        }

        // BigDecimal이면 double로 변환해 세팅한다.
        if (value instanceof BigDecimal bigDecimalValue) {
            cell.setCellValue(bigDecimalValue.doubleValue());
            return;
        }

        // LocalDate이면 문자열로 세팅한다.
        if (value instanceof LocalDate localDateValue) {
            cell.setCellValue(localDateValue.toString());
            return;
        }

        // LocalDateTime이면 문자열로 세팅한다.
        if (value instanceof LocalDateTime localDateTimeValue) {
            cell.setCellValue(localDateTimeValue.toString());
            return;
        }

        // 그 외 타입은 문자열로 변환한다.
        cell.setCellValue(String.valueOf(value));
    }

    /**
     * 컬럼 너비를 설정한다.
     *
     * @param sheet 시트
     * @param columns 컬럼 메타 목록
     */
    private void applyColumnWidth(
            org.apache.poi.ss.usermodel.Sheet sheet,
            List<ExcelColumnMeta> columns
    ) {

        // 컬럼 개수만큼 너비를 설정한다.
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {

            // 컬럼 메타를 가져온다.
            ExcelColumnMeta column = columns.get(columnIndex);

            // 헤더명 길이를 기준으로 기본 너비를 계산한다.
            int headerLength = column.getHeaderName() == null
                    ? 10
                    : column.getHeaderName().length();

            // 최소 15글자 너비를 보장한다.
            int width = Math.max(headerLength + 5, 15);

            // POI 컬럼 너비 단위는 1/256 문자 단위이다.
            sheet.setColumnWidth(columnIndex, width * 256);
        }
    }

    /**
     * 헤더 스타일을 생성한다.
     *
     * @param workbook Workbook
     * @return 헤더 스타일
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {

        // 셀 스타일을 생성한다.
        CellStyle style = workbook.createCellStyle();

        // 가운데 정렬을 적용한다.
        style.setAlignment(HorizontalAlignment.CENTER);

        // 세로 가운데 정렬을 적용한다.
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 배경색을 회색으로 설정한다.
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        // 배경색 채우기 패턴을 설정한다.
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 테두리를 적용한다.
        applyBorder(style);

        // 폰트를 생성한다.
        Font font = workbook.createFont();

        // 굵은 글씨로 설정한다.
        font.setBold(true);

        // 폰트를 스타일에 적용한다.
        style.setFont(font);

        // 스타일을 반환한다.
        return style;
    }

    /**
     * 필수 컬럼 헤더 스타일을 생성한다.
     *
     * @param workbook Workbook
     * @return 필수 헤더 스타일
     */
    private CellStyle createRequiredHeaderStyle(SXSSFWorkbook workbook) {

        // 셀 스타일을 생성한다.
        CellStyle style = workbook.createCellStyle();

        // 가운데 정렬을 적용한다.
        style.setAlignment(HorizontalAlignment.CENTER);

        // 세로 가운데 정렬을 적용한다.
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 필수 컬럼은 노란색 배경으로 표시한다.
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());

        // 배경색 채우기 패턴을 설정한다.
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 테두리를 적용한다.
        applyBorder(style);

        // 폰트를 생성한다.
        Font font = workbook.createFont();

        // 굵은 글씨로 설정한다.
        font.setBold(true);

        // 폰트를 스타일에 적용한다.
        style.setFont(font);

        // 스타일을 반환한다.
        return style;
    }

    /**
     * 본문 셀 스타일을 생성한다.
     *
     * @param workbook Workbook
     * @return 본문 스타일
     */
    private CellStyle createBodyStyle(SXSSFWorkbook workbook) {

        // 셀 스타일을 생성한다.
        CellStyle style = workbook.createCellStyle();

        // 세로 가운데 정렬을 적용한다.
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 테두리를 적용한다.
        applyBorder(style);

        // 스타일을 반환한다.
        return style;
    }

    /**
     * 셀 스타일에 기본 테두리를 적용한다.
     *
     * @param style 셀 스타일
     */
    private void applyBorder(CellStyle style) {

        // 위쪽 테두리.
        style.setBorderTop(BorderStyle.THIN);

        // 아래쪽 테두리.
        style.setBorderBottom(BorderStyle.THIN);

        // 왼쪽 테두리.
        style.setBorderLeft(BorderStyle.THIN);

        // 오른쪽 테두리.
        style.setBorderRight(BorderStyle.THIN);
    }

    /**
     * 시트명을 결정한다.
     *
     * @param sheetName 요청 시트명
     * @return 실제 시트명
     */
    private String resolveSheetName(String sheetName) {

        // 시트명이 있으면 요청 시트명을 사용한다.
        if (StringUtils.hasText(sheetName)) {
            return sheetName;
        }

        // 없으면 기본 시트명을 사용한다.
        return DEFAULT_SHEET_NAME;
    }
}