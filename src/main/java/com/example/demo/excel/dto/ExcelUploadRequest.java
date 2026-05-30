package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 엑셀 업로드 요청 DTO.
 *
 * <p>
 * MultipartFile과 함께 전달되는 업로드 처리 기준 정보이다.
 * </p>
 *
 * <p>
 * 이 DTO는 실제 엑셀 파일 내용이 아니라,
 * 서버가 엑셀 파일을 어떻게 읽고 검증할지에 대한 메타 정보를 담는다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadRequest {

    /**
     * 업로드 대상 시트명.
     *
     * <p>
     * 값이 없으면 첫 번째 시트를 읽는다.
     * </p>
     */
    private String sheetName;

    /**
     * 업로드 검증 기준 컬럼 목록.
     *
     * <p>
     * 템플릿 다운로드에 사용한 ColumnMeta와 동일한 구조를 사용한다.
     * </p>
     *
     * <p>
     * 업로드 시 이 컬럼 목록을 기준으로:
     * 1. 헤더 검증
     * 2. 필수 컬럼 검증
     * 3. 컬럼명 기준 매핑
     * 4. 타입 변환
     * 을 수행한다.
     * </p>
     */
    @Builder.Default
    private List<ExcelColumnMeta> columns = new ArrayList<>();

    /**
     * 업로드 옵션.
     *
     * <p>
     * 헤더 row 위치, 데이터 시작 row 위치, 수식 차단 여부 등을 제어한다.
     * </p>
     */
    @Builder.Default
    private ExcelUploadOption option = ExcelUploadOption.defaultOption();
}