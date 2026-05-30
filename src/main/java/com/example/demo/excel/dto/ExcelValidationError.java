package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 엑셀 업로드 검증 오류 DTO.
 *
 * <p>
 * 업로드된 엑셀 파일의 특정 행, 특정 컬럼에서 발생한 오류를 표현한다.
 * </p>
 *
 * <p>
 * 프론트에서는 이 정보를 이용해 AG Grid에 오류 메시지를 표시하거나,
 * 오류 행 다운로드 기능에 활용할 수 있다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelValidationError {

    /**
     * 엑셀 행 번호.
     *
     * <p>
     * 사용자가 보는 엑셀 기준 행 번호이다.
     * 보통 1부터 시작하는 번호로 반환한다.
     * </p>
     */
    private int rowNo;

    /**
     * 오류가 발생한 필드명.
     *
     * <p>
     * 예:
     * customerCode,
     * qty,
     * amount
     * </p>
     */
    private String field;

    /**
     * 오류가 발생한 헤더명.
     *
     * <p>
     * 예:
     * 고객코드,
     * 수량,
     * 금액
     * </p>
     */
    private String headerName;

    /**
     * 오류 메시지.
     *
     * <p>
     * 예:
     * 수량은 0보다 커야 합니다.
     * 고객코드는 필수입니다.
     * 날짜 형식이 올바르지 않습니다.
     * </p>
     */
    private String message;

    /**
     * 업로드된 원본 값.
     *
     * <p>
     * 사용자가 어떤 값을 넣어서 오류가 났는지 보여주기 위해 사용한다.
     * </p>
     */
    private Object value;
}