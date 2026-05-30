package com.example.demo.excel.sample.dynamicgrid.excel;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelValidationError;
import com.example.demo.excel.validator.ExcelRowValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 엑셀 샘플 다이나믹 그리드 업로드 업무 검증 Validator.
 *
 * <p>
 * 공통 ExcelUploadService는 헤더 검증, 필수값 검증, 타입 변환, 수식 차단을 담당한다.
 * 이 Validator는 다이나믹 그리드 샘플 화면의 업무 규칙을 검증한다.
 * </p>
 *
 * <p>
 * 샘플 검증 기준:
 * 1. 기본 컬럼 값 보강 검증
 * 2. sort3/sort4/sort5는 0 또는 1만 허용
 * 3. 동적 컬럼 값은 음수 불가
 * 4. Ratio 컬럼은 0 이상 100 이하만 허용
 * </p>
 */
@Component
public class SampleDynamicGridExcelValidator implements ExcelRowValidator {

    /**
     * 기본 컬럼: 구분.
     */
    private static final String FIELD_CATEGORY_NAME = "categoryName";

    /**
     * 기본 컬럼: APP.
     */
    private static final String FIELD_APP_NAME = "appName";

    /**
     * 기본 컬럼: GA/NGA.
     */
    private static final String FIELD_GA_NGA_TYPE = "gaNgaType";

    /**
     * 기본 컬럼: CUSTOMER.
     */
    private static final String FIELD_CUSTOMER_NAME = "customerName";

    /**
     * 계층 스타일 플래그 sort3.
     */
    private static final String FIELD_SORT3 = "sort3";

    /**
     * 계층 스타일 플래그 sort4.
     */
    private static final String FIELD_SORT4 = "sort4";

    /**
     * 계층 스타일 플래그 sort5.
     */
    private static final String FIELD_SORT5 = "sort5";

    /**
     * Ratio Dimension prefix.
     */
    private static final String RATIO_PREFIX = "Ratio_Q";

    /**
     * 엑셀 row 데이터를 업무 기준으로 검증한다.
     *
     * @param rowData 업로드 row 데이터
     * @param rowNo 엑셀 사용자 기준 행 번호
     * @param columns 컬럼 메타 목록
     * @param allRows 현재까지 처리된 row 목록
     * @return 검증 오류 목록
     */
    @Override
    public List<ExcelValidationError> validate(
            Map<String, Object> rowData,
            int rowNo,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> allRows
    ) {

        // 오류 목록을 생성한다.
        List<ExcelValidationError> errors = new ArrayList<>();

        // 기본 컬럼 필수 검증을 수행한다.
        validateRequiredBaseField(errors, rowData, rowNo, FIELD_CATEGORY_NAME, "구분");
        validateRequiredBaseField(errors, rowData, rowNo, FIELD_APP_NAME, "APP");
        validateRequiredBaseField(errors, rowData, rowNo, FIELD_GA_NGA_TYPE, "GA/NGA");
        validateRequiredBaseField(errors, rowData, rowNo, FIELD_CUSTOMER_NAME, "CUSTOMER");

        // sort 필드 검증을 수행한다.
        validateSortFlag(errors, rowData, rowNo, FIELD_SORT3, "sort3");
        validateSortFlag(errors, rowData, rowNo, FIELD_SORT4, "sort4");
        validateSortFlag(errors, rowData, rowNo, FIELD_SORT5, "sort5");

        // 동적 컬럼 값 검증을 수행한다.
        validateDynamicValues(errors, rowData, rowNo);

        // 오류 목록을 반환한다.
        return errors;
    }

    /**
     * 기본 컬럼 필수값을 검증한다.
     *
     * @param errors 오류 목록
     * @param rowData row 데이터
     * @param rowNo 엑셀 row 번호
     * @param field 필드명
     * @param headerName 헤더명
     */
    private void validateRequiredBaseField(
            List<ExcelValidationError> errors,
            Map<String, Object> rowData,
            int rowNo,
            String field,
            String headerName
    ) {

        // 값을 가져온다.
        Object value = rowData.get(field);

        // 값이 있으면 정상이다.
        if (hasValue(value)) {
            return;
        }

        // 필수값 오류를 추가한다.
        errors.add(ExcelValidationError.builder()
                .rowNo(rowNo)
                .field(field)
                .headerName(headerName)
                .message(headerName + "은(는) 필수값입니다.")
                .value(value)
                .build());
    }

    /**
     * sort 플래그 값을 검증한다.
     *
     * @param errors 오류 목록
     * @param rowData row 데이터
     * @param rowNo 엑셀 row 번호
     * @param field 필드명
     * @param headerName 헤더명
     */
    private void validateSortFlag(
            List<ExcelValidationError> errors,
            Map<String, Object> rowData,
            int rowNo,
            String field,
            String headerName
    ) {

        // 값을 가져온다.
        Object value = rowData.get(field);

        // 값이 비어 있으면 검증하지 않는다.
        if (value == null) {
            return;
        }

        // Integer로 변환한다.
        Integer intValue = toInteger(value);

        // 0 또는 1이면 정상이다.
        if (Integer.valueOf(0).equals(intValue) || Integer.valueOf(1).equals(intValue)) {
            return;
        }

        // 오류를 추가한다.
        errors.add(ExcelValidationError.builder()
                .rowNo(rowNo)
                .field(field)
                .headerName(headerName)
                .message(headerName + " 값은 0 또는 1만 입력할 수 있습니다.")
                .value(value)
                .build());
    }

    /**
     * 동적 컬럼 값을 검증한다.
     *
     * @param errors 오류 목록
     * @param rowData row 데이터
     * @param rowNo 엑셀 row 번호
     */
    private void validateDynamicValues(
            List<ExcelValidationError> errors,
            Map<String, Object> rowData,
            int rowNo
    ) {

        // rowData 전체 필드를 순회한다.
        for (Map.Entry<String, Object> entry : rowData.entrySet()) {

            // field명을 가져온다.
            String field = entry.getKey();

            // 동적 값 컬럼이 아니면 건너뛴다.
            if (!isDynamicValueField(field)) {
                continue;
            }

            // 값을 가져온다.
            Object value = entry.getValue();

            // 값이 비어 있으면 검증하지 않는다.
            if (value == null) {
                continue;
            }

            // BigDecimal로 변환한다.
            BigDecimal decimalValue = toBigDecimal(value);

            // 숫자로 변환할 수 없으면 오류를 추가한다.
            if (decimalValue == null) {
                errors.add(ExcelValidationError.builder()
                        .rowNo(rowNo)
                        .field(field)
                        .headerName(field)
                        .message(field + " 값은 숫자여야 합니다.")
                        .value(value)
                        .build());
                continue;
            }

            // 음수는 허용하지 않는다.
            if (decimalValue.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(ExcelValidationError.builder()
                        .rowNo(rowNo)
                        .field(field)
                        .headerName(field)
                        .message(field + " 값은 0 이상이어야 합니다.")
                        .value(value)
                        .build());
            }

            // Ratio 컬럼은 0~100 범위만 허용한다.
            if (field.startsWith(RATIO_PREFIX)
                    && decimalValue.compareTo(BigDecimal.ZERO) >= 0
                    && decimalValue.compareTo(BigDecimal.valueOf(100)) > 0) {

                errors.add(ExcelValidationError.builder()
                        .rowNo(rowNo)
                        .field(field)
                        .headerName(field)
                        .message(field + " 값은 100 이하이어야 합니다.")
                        .value(value)
                        .build());
            }
        }
    }

    /**
     * 동적 값 컬럼 여부를 판단한다.
     *
     * @param field 필드명
     * @return 동적 값 컬럼이면 true
     */
    private boolean isDynamicValueField(String field) {

        // field가 없으면 false.
        if (!StringUtils.hasText(field)) {
            return false;
        }

        // 시스템 필드는 제외한다.
        if (field.startsWith("_")) {
            return false;
        }

        // 기본 컬럼은 제외한다.
        if (FIELD_CATEGORY_NAME.equals(field)
                || FIELD_APP_NAME.equals(field)
                || FIELD_GA_NGA_TYPE.equals(field)
                || FIELD_CUSTOMER_NAME.equals(field)
                || FIELD_SORT3.equals(field)
                || FIELD_SORT4.equals(field)
                || FIELD_SORT5.equals(field)) {
            return false;
        }

        // 동적 컬럼은 _Q 포함 여부로 판단한다.
        return field.contains("_Q");
    }

    /**
     * 값 존재 여부를 판단한다.
     *
     * @param value 값
     * @return 값이 있으면 true
     */
    private boolean hasValue(Object value) {

        // null이면 값이 없다.
        if (value == null) {
            return false;
        }

        // 문자열이면 공백 여부를 확인한다.
        if (value instanceof String stringValue) {
            return StringUtils.hasText(stringValue);
        }

        // 그 외 타입은 값이 있는 것으로 판단한다.
        return true;
    }

    /**
     * Object 값을 Integer로 변환한다.
     *
     * @param value 값
     * @return Integer 값
     */
    private Integer toInteger(Object value) {

        // null이면 null.
        if (value == null) {
            return null;
        }

        // 이미 Integer이면 그대로 반환.
        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        // Number이면 intValue로 변환.
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        // 문자열이면 정수 변환을 시도한다.
        try {
            String stringValue = String.valueOf(value);

            if (!StringUtils.hasText(stringValue)) {
                return null;
            }

            return Integer.valueOf(stringValue.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Object 값을 BigDecimal로 변환한다.
     *
     * @param value 값
     * @return BigDecimal 값
     */
    private BigDecimal toBigDecimal(Object value) {

        // null이면 null.
        if (value == null) {
            return null;
        }

        // 이미 BigDecimal이면 그대로 반환.
        if (value instanceof BigDecimal bigDecimalValue) {
            return bigDecimalValue;
        }

        // Number이면 문자열 기반으로 BigDecimal 변환.
        if (value instanceof Number numberValue) {
            return new BigDecimal(numberValue.toString());
        }

        // 문자열이면 콤마 제거 후 BigDecimal 변환을 시도한다.
        try {
            String stringValue = String.valueOf(value);

            if (!StringUtils.hasText(stringValue)) {
                return null;
            }

            return new BigDecimal(stringValue.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }
}