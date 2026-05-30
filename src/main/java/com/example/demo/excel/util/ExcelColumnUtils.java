package com.example.demo.excel.util;

import com.example.demo.excel.dto.ExcelColumnMeta;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 엑셀 컬럼 메타 처리 유틸 클래스.
 *
 * <p>
 * 컬럼 정렬, 숨김 컬럼 제외, 유효 컬럼 필터링 등
 * 다운로드/업로드에서 공통으로 사용하는 컬럼 처리 로직을 담당한다.
 * </p>
 */
public final class ExcelColumnUtils {

    /**
     * 유틸 클래스이므로 외부에서 생성하지 못하게 막는다.
     */
    private ExcelColumnUtils() {
    }

    /**
     * 다운로드 또는 업로드 처리에 사용할 컬럼 목록을 정리한다.
     *
     * <p>
     * 처리 기준:
     * 1. null 컬럼 제거
     * 2. field가 없는 컬럼 제거
     * 3. headerName이 없는 컬럼 제거
     * 4. excludeHiddenColumns=true이면 hidden 컬럼 제거
     * 5. order 기준 정렬
     * </p>
     *
     * @param columns 원본 컬럼 목록
     * @param excludeHiddenColumns 숨김 컬럼 제외 여부
     * @return 정리된 컬럼 목록
     */
    public static List<ExcelColumnMeta> normalizeColumns(
            List<ExcelColumnMeta> columns,
            boolean excludeHiddenColumns
    ) {

        // 컬럼 목록이 null이면 빈 리스트를 반환한다.
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }

        // 유효한 컬럼만 필터링하고 order 기준으로 정렬한다.
        return columns.stream()
                .filter(Objects::nonNull)
                .filter(ExcelColumnUtils::hasTextField)
                .filter(ExcelColumnUtils::hasTextHeaderName)
                .filter(column -> includeByHiddenCondition(column, excludeHiddenColumns))
                .sorted(columnOrderComparator())
                .toList();
    }

    /**
     * field 값이 있는지 확인한다.
     *
     * @param column 컬럼 메타
     * @return field 값이 있으면 true
     */
    private static boolean hasTextField(ExcelColumnMeta column) {
        return hasText(column.getField());
    }

    /**
     * headerName 값이 있는지 확인한다.
     *
     * @param column 컬럼 메타
     * @return headerName 값이 있으면 true
     */
    private static boolean hasTextHeaderName(ExcelColumnMeta column) {
        return hasText(column.getHeaderName());
    }

    /**
     * hidden 조건에 따라 컬럼 포함 여부를 판단한다.
     *
     * @param column 컬럼 메타
     * @param excludeHiddenColumns 숨김 컬럼 제외 여부
     * @return 포함 대상이면 true
     */
    private static boolean includeByHiddenCondition(
            ExcelColumnMeta column,
            boolean excludeHiddenColumns
    ) {

        // 숨김 컬럼 제외 옵션이 꺼져 있으면 항상 포함한다.
        if (!excludeHiddenColumns) {
            return true;
        }

        // 숨김 컬럼 제외 옵션이 켜져 있으면 hidden=false인 컬럼만 포함한다.
        return !column.isHidden();
    }

    /**
     * 컬럼 정렬 Comparator를 반환한다.
     *
     * <p>
     * order가 null이면 가장 뒤로 보낸다.
     * order가 같으면 원래 stream 순서를 최대한 유지한다.
     * </p>
     *
     * @return 컬럼 정렬 Comparator
     */
    private static Comparator<ExcelColumnMeta> columnOrderComparator() {
        return Comparator.comparing(
                ExcelColumnMeta::getOrder,
                Comparator.nullsLast(Integer::compareTo)
        );
    }

    /**
     * 문자열 값이 존재하는지 확인한다.
     *
     * @param value 문자열
     * @return null이 아니고 공백이 아니면 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}