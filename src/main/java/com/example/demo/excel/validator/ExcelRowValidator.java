package com.example.demo.excel.validator;

import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelValidationError;

import java.util.List;
import java.util.Map;

/**
 * 엑셀 업로드 업무 검증 인터페이스.
 *
 * <p>
 * 공통 ExcelUploadService는 헤더, 필수값, 타입 변환 같은 공통 검증만 담당한다.
 * 수량 > 0, 고객코드 존재 여부, 코드값 검증 같은 업무별 검증은 이 인터페이스를 구현해서 처리한다.
 * </p>
 */
public interface ExcelRowValidator {

    /**
     * 엑셀 업로드 row 데이터를 업무 기준으로 검증한다.
     *
     * @param rowData     업로드 row 데이터
     * @param rowNo       엑셀 사용자 기준 행 번호
     * @param columns     컬럼 메타 목록
     * @param allRows     전체 업로드 row 목록
     * @return 검증 오류 목록
     */
    List<ExcelValidationError> validate(
            Map<String, Object> rowData,
            int rowNo,
            List<ExcelColumnMeta> columns,
            List<Map<String, Object>> allRows
    );
}