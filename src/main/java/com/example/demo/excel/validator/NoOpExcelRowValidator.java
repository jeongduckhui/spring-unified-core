package com.example.demo.excel.validator;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelValidationError;

import java.util.List;
import java.util.Map;

/**
 * 아무 업무 검증도 수행하지 않는 기본 Validator.
 *
 * <p>
 * 업무별 Validator가 없는 경우 사용할 수 있다.
 * </p>
 */
public class NoOpExcelRowValidator implements ExcelRowValidator {

    /**
     * 업무 검증을 수행하지 않고 빈 오류 목록을 반환한다.
     *
     * @param rowData 업로드 row 데이터
     * @param rowNo   엑셀 사용자 기준 행 번호
     * @param columns 컬럼 메타 목록
     * @param allRows 전체 업로드 row 목록
     * @return 빈 오류 목록
     */
    @Override
    public List<ExcelValidationError> validate(
            Map<String, Object> rowData,
            int rowNo,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> allRows
    ) {
        return List.of();
    }
}