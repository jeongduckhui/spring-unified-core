package com.example.demo.excel.sample;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelValidationError;
import com.example.demo.excel.validator.ExcelRowValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sales 엑셀 업로드 업무 검증 Validator.
 */
@Component
public class SalesExcelRowValidator implements ExcelRowValidator {

    /**
     * Sales 업로드 row 업무 검증.
     *
     * @param rowData 업로드 row 데이터
     * @param rowNo   엑셀 사용자 기준 행 번호
     * @param columns 컬럼 메타 목록
     * @param allRows 전체 업로드 row 목록
     * @return 검증 오류 목록
     */
    @Override
    public List<ExcelValidationError> validate(
            Map<String, Object> rowData,
            int rowNo,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> allRows
    ) {

        List<ExcelValidationError> errors = new ArrayList<>();

        Object qtyValue = rowData.get("qty");

        if (qtyValue instanceof Number number) {
            BigDecimal qty = new BigDecimal(number.toString());

            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(ExcelValidationError.builder()
                        .rowNo(rowNo)
                        .field("qty")
                        .headerName("수량")
                        .message("수량은 0보다 커야 합니다.")
                        .value(qtyValue)
                        .build());
            }
        }

        return errors;
    }
}