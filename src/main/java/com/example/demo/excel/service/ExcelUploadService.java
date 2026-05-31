package com.example.demo.excel.service;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelUploadOption;
import com.example.demo.excel.dto.ExcelUploadRequest;
import com.example.demo.excel.dto.ExcelUploadResult;
import com.example.demo.excel.dto.ExcelValidationError;
import com.example.demo.excel.util.ExcelCellReadUtils;
import com.example.demo.excel.util.ExcelColumnUtils;
import com.example.demo.excel.util.ExcelHeaderUtils;
import com.example.demo.excel.validator.ExcelRowValidator;
import com.example.demo.excel.validator.NoOpExcelRowValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExcelUploadService {

    private static final String ROW_STATUS_FIELD = "_rowStatus";
    private static final String ROW_ERROR_MESSAGES_FIELD = "_errorMessages";
    private static final String ROW_STATUS_NORMAL = "NORMAL";
    private static final String ROW_STATUS_ERROR = "ERROR";

    /**
     * 기본 엑셀 업로드 메서드.
     *
     * <p>
     * 업무별 검증 Validator를 별도로 넘기지 않는 경우 사용한다.
     * 이 경우 헤더 검증, 필수값 검증, 타입 변환 같은 공통 검증만 수행한다.
     * </p>
     *
     * @param file 업로드 엑셀 파일
     * @param request 업로드 요청 정보
     * @return 업로드 검증 결과
     */
    public ExcelUploadResult upload(
            MultipartFile file,
            ExcelUploadRequest request
    ) {

        // 업무 검증이 필요 없는 기본 Validator를 사용한다.
        return upload(file, request, new NoOpExcelRowValidator());
    }

    /**
     * 업무별 Validator를 포함한 엑셀 업로드 메서드.
     *
     * <p>
     * 공통 검증 이후 업무별 검증을 추가로 수행하고 싶을 때 사용한다.
     * 예:
     * 수량 > 0,
     * 고객코드 존재 여부,
     * 코드값 검증,
     * 중복 데이터 검증 등.
     * </p>
     *
     * @param file 업로드 엑셀 파일
     * @param request 업로드 요청 정보
     * @param rowValidator 업무별 row 검증 Validator
     * @return 업로드 검증 결과
     */
    public ExcelUploadResult upload(
            MultipartFile file,
            ExcelUploadRequest request,
            ExcelRowValidator rowValidator
    ) {

        // 업로드 파일 자체를 검증한다.
        validateFile(file);

        // 요청 컬럼 목록을 먼저 정리한다.
        // 이유:
        // 업로드 옵션을 자동 생성하려면 ColumnMeta의 headerPath를 보고
        // 헤더가 1단인지, 2단인지, 3단인지 판단해야 하기 때문이다.
        List<ExcelColumnMeta> columns = ExcelColumnUtils.normalizeColumns(
                request == null ? null : request.getColumns(),
                false
        );

        // 업로드 옵션을 결정한다.
        // request.option이 있으면 요청 옵션을 우선 사용하고,
        // request.option이 없으면 columns의 headerPath 기준으로 자동 생성한다.
        ExcelUploadOption option = resolveOption(request, columns);

        // 업무 Validator가 null이면 기본 Validator로 대체한다.
        ExcelRowValidator resolvedRowValidator = resolveRowValidator(rowValidator);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            // 요청 시트명 기준 또는 첫 번째 시트를 가져온다.
            Sheet sheet = resolveSheet(
                    workbook,
                    request == null ? null : request.getSheetName()
            );

            // 업로드 파일의 헤더 정보를 읽는다.
            Map<Integer, List<String>> uploadHeaderMap = ExcelHeaderUtils.readHeaderMap(sheet, option);

            // 전체 오류 목록을 생성한다.
            List<ExcelValidationError> errors = new ArrayList<>();

            // 헤더 검증 오류를 추가한다.
            errors.addAll(
                    ExcelHeaderUtils.validateHeaders(
                            uploadHeaderMap,
                            columns,
                            option.isUseMultiHeader()
                    )
            );

            // 엑셀 컬럼 index와 ColumnMeta를 매핑한다.
            Map<Integer, ExcelColumnMeta> columnIndexMap =
                    ExcelHeaderUtils.createColumnIndexMap(
                            uploadHeaderMap,
                            columns,
                            option.isUseMultiHeader()
                    );

            // 엑셀 데이터 row를 읽고 공통 검증 + 업무 검증을 수행한다.
            List<Map<String, Object>> rows = readDataRows(
                    sheet,
                    option,
                    columnIndexMap,
                    columns,
                    errors,
                    resolvedRowValidator
            );

            // 전체 건수를 계산한다.
            int totalCount = rows.size();

            // 오류 건수를 계산한다.
            int errorCount = countErrorRows(rows);

            // 정상 건수를 계산한다.
            int successCount = totalCount - errorCount;

            // errors가 비어 있으면 전체 성공이다.
            boolean success = errors.isEmpty();

            log.info("columns size={}", columns.size());
            log.info("columnIndexMap size={}", columnIndexMap.size());
            log.info("uploadHeaderMap={}", uploadHeaderMap);

            // 업로드 결과를 반환한다.
            return ExcelUploadResult.builder()
                    .totalCount(totalCount)
                    .successCount(successCount)
                    .errorCount(errorCount)
                    .success(success)
                    .rows(rows)
                    .errors(errors)
                    .build();

        } catch (IOException e) {
            log.error("엑셀 업로드 파일을 읽는 중 오류가 발생했습니다.", e);
            throw new IllegalStateException("엑셀 업로드 파일을 읽는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("엑셀 업로드 처리 중 오류가 발생했습니다.", e);
            throw new IllegalStateException("엑셀 업로드 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 엑셀 데이터 row를 읽고 검증한다.
     *
     * <p>
     * 이 메서드에서 수행하는 작업:
     * 1. 빈 행 무시
     * 2. 수식 셀 차단
     * 3. 타입 변환
     * 4. 필수값 검증
     * 5. 업무별 Validator 실행
     * 6. row 상태값 생성
     * </p>
     *
     * @param sheet 엑셀 시트
     * @param option 업로드 옵션
     * @param columnIndexMap 엑셀 컬럼 index와 ColumnMeta 매핑
     * @param columns 전체 컬럼 메타 목록
     * @param errors 전체 오류 목록
     * @param rowValidator 업무별 row 검증 Validator
     * @return Grid 표시용 row 데이터 목록
     */
    private List<Map<String, Object>> readDataRows(
            Sheet sheet,
            ExcelUploadOption option,
            Map<Integer, ExcelColumnMeta> columnIndexMap,
            List<ExcelColumnMeta> columns,
            List<ExcelValidationError> errors,
            ExcelRowValidator rowValidator
    ) {

        // Grid에 표시할 row 목록을 생성한다.
        List<Map<String, Object>> rows = new ArrayList<>();

        // 마지막 row index를 구한다.
        int lastRowNum = sheet.getLastRowNum();

        log.info(
                "dataStartRowIndex={}, lastRowNum={}",
                option.getDataStartRowIndex(),
                lastRowNum
        );

        // 데이터 시작 row부터 마지막 row까지 순회한다.
        for (int rowIndex = option.getDataStartRowIndex(); rowIndex <= lastRowNum; rowIndex++) {

            log.info("rowIndex={}", rowIndex);

            // 현재 row를 가져온다.
            Row row = sheet.getRow(rowIndex);

            // 빈 행 무시 옵션이 켜져 있고 현재 행이 빈 행이면 건너뛴다.
            if (option.isIgnoreEmptyRow() && isEmptyRow(row, columnIndexMap)) {
                continue;
            }

            // 현재 row 데이터를 담을 Map을 생성한다.
            Map<String, Object> rowData = new LinkedHashMap<>();

            // 현재 row의 오류 메시지 목록을 생성한다.
            List<String> rowErrorMessages = new ArrayList<>();

            // 엑셀 컬럼 index 기준으로 셀을 읽는다.
            for (Map.Entry<Integer, ExcelColumnMeta> entry : columnIndexMap.entrySet()) {

                // 엑셀 셀 index를 가져온다.
                int cellIndex = entry.getKey();

                // 컬럼 메타를 가져온다.
                ExcelColumnMeta column = entry.getValue();

                // 현재 셀을 가져온다.
                Cell cell = row == null ? null : row.getCell(cellIndex);

                // 수식 차단 옵션이 켜져 있고 현재 셀이 수식 셀이면 오류 처리한다.
                if (option.isBlockFormula() && ExcelCellReadUtils.isFormulaCell(cell)) {

                    // 수식 셀 오류를 생성한다.
                    ExcelValidationError error = ExcelValidationError.builder()
                            .rowNo(rowIndex + 1)
                            .field(column.getField())
                            .headerName(column.getHeaderName())
                            .message("수식 셀은 업로드할 수 없습니다.")
                            .value(ExcelCellReadUtils.readAsString(cell))
                            .build();

                    // 전체 오류 목록에 추가한다.
                    errors.add(error);

                    // 현재 row 오류 메시지에 추가한다.
                    rowErrorMessages.add(error.getMessage());

                    // 해당 필드 값은 null로 넣는다.
                    rowData.put(column.getField(), null);

                    // 다음 셀로 넘어간다.
                    continue;
                }

                // 변환된 셀 값을 담을 변수.
                Object value;

                try {

                    // 셀 값을 ColumnMeta의 dataType 기준으로 읽는다.
                    value = ExcelCellReadUtils.readValue(cell, column.getDataType());

                } catch (Exception e) {

                    // 변환 실패 시 원본 문자열 값을 읽는다.
                    String rawValue = ExcelCellReadUtils.readAsString(cell);

                    // 타입 변환 오류를 생성한다.
                    ExcelValidationError error = ExcelValidationError.builder()
                            .rowNo(rowIndex + 1)
                            .field(column.getField())
                            .headerName(column.getHeaderName())
                            .message("셀 값을 " + column.getDataType() + " 타입으로 변환할 수 없습니다.")
                            .value(rawValue)
                            .build();

                    // 전체 오류 목록에 추가한다.
                    errors.add(error);

                    // 현재 row 오류 메시지에 추가한다.
                    rowErrorMessages.add(error.getMessage());

                    // 변환 실패한 경우 원본 문자열 값을 rowData에 넣는다.
                    rowData.put(column.getField(), rawValue);

                    // 다음 셀로 넘어간다.
                    continue;
                }

                // 필수 컬럼인데 값이 비어 있으면 오류 처리한다.
                if (column.isRequired() && isEmptyValue(value)) {

                    // 필수값 오류를 생성한다.
                    ExcelValidationError error = ExcelValidationError.builder()
                            .rowNo(rowIndex + 1)
                            .field(column.getField())
                            .headerName(column.getHeaderName())
                            .message(column.getHeaderName() + "은(는) 필수값입니다.")
                            .value(value)
                            .build();

                    // 전체 오류 목록에 추가한다.
                    errors.add(error);

                    // 현재 row 오류 메시지에 추가한다.
                    rowErrorMessages.add(error.getMessage());
                }

                // rowData에 변환된 값을 넣는다.
                rowData.put(column.getField(), value);
            }

            // 업무별 Validator를 실행한다.
            List<ExcelValidationError> businessErrors =
                    rowValidator.validate(
                            rowData,
                            rowIndex + 1,
                            columns,
                            rows
                    );

            // 업무 검증 오류가 있으면 전체 오류 목록과 현재 row 메시지 목록에 추가한다.
            if (businessErrors != null && !businessErrors.isEmpty()) {

                // 전체 오류 목록에 업무 오류를 추가한다.
                errors.addAll(businessErrors);

                // 현재 row 오류 메시지 목록에 업무 오류 메시지를 추가한다.
                for (ExcelValidationError businessError : businessErrors) {
                    rowErrorMessages.add(businessError.getMessage());
                }
            }

            // 현재 row에 오류가 없으면 정상 상태로 추가한다.
            if (rowErrorMessages.isEmpty()) {

                // row 상태를 정상으로 표시한다.
                rowData.put(ROW_STATUS_FIELD, ROW_STATUS_NORMAL);

                // 오류 메시지는 빈 문자열로 표시한다.
                rowData.put(ROW_ERROR_MESSAGES_FIELD, "");

                // rows에 추가한다.
                rows.add(rowData);

                // 다음 row로 넘어간다.
                continue;
            }

            // 현재 row에 오류가 있으면 오류 상태로 표시한다.
            rowData.put(ROW_STATUS_FIELD, ROW_STATUS_ERROR);

            // 여러 오류 메시지를 줄바꿈으로 합친다.
            rowData.put(ROW_ERROR_MESSAGES_FIELD, String.join("\n", rowErrorMessages));

            // 오류 행 포함 옵션이 켜져 있으면 rows에 추가한다.
            if (option.isIncludeErrorRows()) {
                rows.add(rowData);
            }
        }

        // Grid 표시용 rows를 반환한다.
        return rows;
    }

    /**
     * 현재 row가 빈 행인지 판단한다.
     *
     * @param row 엑셀 row
     * @param columnIndexMap 엑셀 컬럼 index와 ColumnMeta 매핑
     * @return 빈 행이면 true
     */
    private boolean isEmptyRow(
            Row row,
            Map<Integer, ExcelColumnMeta> columnIndexMap
    ) {

        // row 자체가 없으면 빈 행이다.
        if (row == null) {
            return true;
        }

        // 매핑된 컬럼 기준으로 셀을 확인한다.
        for (Integer cellIndex : columnIndexMap.keySet()) {

            // 현재 셀을 가져온다.
            Cell cell = row.getCell(cellIndex);

            // 하나라도 빈 셀이 아니면 빈 행이 아니다.
            if (!ExcelCellReadUtils.isBlankCell(cell)) {
                return false;
            }
        }

        // 모든 대상 셀이 비어 있으면 빈 행이다.
        return true;
    }

    /**
     * 값이 비어 있는지 판단한다.
     *
     * @param value 값
     * @return 비어 있으면 true
     */
    private boolean isEmptyValue(Object value) {

        // null이면 빈 값이다.
        if (value == null) {
            return true;
        }

        // 문자열이면 공백 여부를 확인한다.
        if (value instanceof String stringValue) {
            return !StringUtils.hasText(stringValue);
        }

        // 그 외 타입은 값이 있는 것으로 판단한다.
        return false;
    }

    /**
     * 오류 row 개수를 계산한다.
     *
     * @param rows Grid 표시용 rows
     * @return 오류 row 개수
     */
    private int countErrorRows(List<Map<String, Object>> rows) {

        // 오류 row 개수.
        int count = 0;

        // rows를 순회한다.
        for (Map<String, Object> row : rows) {

            // row 상태값을 가져온다.
            Object rowStatus = row.get(ROW_STATUS_FIELD);

            // 오류 상태이면 count 증가.
            if (ROW_STATUS_ERROR.equals(rowStatus)) {
                count++;
            }
        }

        // 오류 row 개수를 반환한다.
        return count;
    }

    /**
     * 업로드 파일을 검증한다.
     *
     * @param file 업로드 파일
     */
    private void validateFile(MultipartFile file) {

        // 파일이 없거나 비어 있으면 오류.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 엑셀 파일이 없습니다.");
        }

        // 원본 파일명을 가져온다.
        String originalFilename = file.getOriginalFilename();

        // 원본 파일명이 없으면 오류.
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("업로드 파일명이 없습니다.");
        }

        // 파일명을 소문자로 변환한다.
        String lowerFileName = originalFilename.toLowerCase();

        // xlsx 또는 xls가 아니면 오류.
        if (!lowerFileName.endsWith(".xlsx") && !lowerFileName.endsWith(".xls")) {
            throw new IllegalArgumentException("엑셀 파일만 업로드할 수 있습니다.");
        }
    }

    /**
     * 업로드 옵션을 결정한다.
     *
     * <p>
     * 우선순위:
     * 1. 요청에서 option을 직접 넘긴 경우: 요청 option을 그대로 사용한다.
     * 2. 요청 option이 없는 경우: columns의 headerPath를 기준으로 자동 계산한다.
     * </p>
     *
     * @param request 업로드 요청
     * @param columns 업로드 컬럼 메타 목록
     * @return 업로드 옵션
     */
    private ExcelUploadOption resolveOption(
            ExcelUploadRequest request,
            List<ExcelColumnMeta> columns
    ) {

        // 요청에서 option을 직접 넘긴 경우에는 그 값을 우선 사용한다.
        // 프론트에서 headerEndRowIndex, dataStartRowIndex를 명확히 내려보낸 경우
        // 백엔드가 임의로 덮어쓰지 않기 위함이다.
        if (request != null && request.getOption() != null) {
            return request.getOption();
        }

        // option이 없으면 columns의 headerPath를 기준으로 자동 생성한다.
        return ExcelUploadOption.fromColumns(columns, false);
    }

    /**
     * 업무별 Validator를 결정한다.
     *
     * @param rowValidator 요청 Validator
     * @return 사용할 Validator
     */
    private ExcelRowValidator resolveRowValidator(ExcelRowValidator rowValidator) {

        // Validator가 없으면 기본 NoOp Validator를 사용한다.
        if (rowValidator == null) {
            return new NoOpExcelRowValidator();
        }

        // 요청 Validator를 그대로 사용한다.
        return rowValidator;
    }

    /**
     * 업로드 대상 시트를 결정한다.
     *
     * @param workbook Workbook
     * @param sheetName 요청 시트명
     * @return 대상 Sheet
     */
    private Sheet resolveSheet(
            Workbook workbook,
            String sheetName
    ) {

        // 시트가 하나도 없으면 오류.
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("엑셀 파일에 시트가 존재하지 않습니다.");
        }

        // 요청 시트명이 있으면 해당 시트를 찾는다.
        if (StringUtils.hasText(sheetName)) {

            // 시트명으로 시트를 가져온다.
            Sheet sheet = workbook.getSheet(sheetName);

            // 해당 시트가 없으면 오류.
            if (sheet == null) {
                throw new IllegalArgumentException("요청한 시트가 존재하지 않습니다. sheetName=" + sheetName);
            }

            // 찾은 시트를 반환한다.
            return sheet;
        }

        // 요청 시트명이 없으면 첫 번째 시트를 반환한다.
        return workbook.getSheetAt(0);
    }
}