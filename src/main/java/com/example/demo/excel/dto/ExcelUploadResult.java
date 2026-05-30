package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 엑셀 업로드 결과 DTO.
 *
 * <p>
 * 엑셀 업로드 후 즉시 DB에 저장하지 않고,
 * 검증 결과를 프론트 Grid에 표시하기 위한 응답 구조이다.
 * </p>
 *
 * <p>
 * 처리 흐름:
 * 업로드
 * → 서버 검증
 * → Grid 표시
 * → 사용자 확인
 * → 저장
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResult {

    /**
     * 전체 데이터 건수.
     *
     * <p>
     * 정상/오류 여부와 관계없이 엑셀에서 읽은 전체 데이터 행 수이다.
     * </p>
     */
    private int totalCount;

    /**
     * 정상 데이터 건수.
     */
    private int successCount;

    /**
     * 오류 데이터 건수.
     */
    private int errorCount;

    /**
     * 업로드 성공 여부.
     *
     * <p>
     * true:
     * 전체 검증 통과
     *
     * false:
     * 헤더 오류, 필수값 오류, 데이터 오류 등이 존재
     * </p>
     */
    private boolean success;

    /**
     * Grid에 표시할 전체 row 데이터.
     *
     * <p>
     * 정상 데이터와 오류 데이터를 모두 포함한다.
     * </p>
     *
     * <p>
     * 동적 컬럼을 지원하기 위해 DTO가 아니라 Map 구조를 사용한다.
     * </p>
     */
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 검증 오류 목록.
     *
     * <p>
     * 특정 행, 특정 필드에서 발생한 오류 정보를 담는다.
     * </p>
     */
    @Builder.Default
    private List<ExcelValidationError> errors = new ArrayList<>();
}