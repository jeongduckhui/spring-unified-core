package com.example.demo.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * 엑셀 업로드 옵션 DTO.
 *
 * <p>
 * 엑셀 파일을 읽을 때 필요한 위치 정보와 검증 옵션을 담는다.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadOption {

    /**
     * 다중 헤더 사용 여부.
     *
     * <p>
     * true이면 여러 줄의 헤더를 조합해서 실제 컬럼명을 판단한다.
     * 예:
     * 2025 / 1Q → 2025_Q1
     * </p>
     */
    private boolean useMultiHeader;

    /**
     * 헤더 시작 row index.
     *
     * <p>
     * 0부터 시작한다.
     * 일반적인 엑셀에서 첫 번째 행이 헤더이면 0이다.
     * </p>
     */
    private int headerStartRowIndex;

    /**
     * 헤더 종료 row index.
     *
     * <p>
     * 0부터 시작한다.
     * 단일 헤더이면 headerStartRowIndex와 같은 값을 사용한다.
     * 2단 헤더이면 보통 1이다.
     * </p>
     */
    private int headerEndRowIndex;

    /**
     * 데이터 시작 row index.
     *
     * <p>
     * 0부터 시작한다.
     * 단일 헤더이면 보통 1,
     * 2단 헤더이면 보통 2,
     * 예제 행을 제외하고 업로드한다면 그 다음 행으로 설정할 수 있다.
     * </p>
     */
    private int dataStartRowIndex;

    /**
     * 빈 행 무시 여부.
     *
     * <p>
     * true이면 모든 셀이 비어 있는 행은 업로드 대상에서 제외한다.
     * </p>
     */
    private boolean ignoreEmptyRow;

    /**
     * 수식 셀 차단 여부.
     *
     * <p>
     * true이면 CellType.FORMULA 셀이 발견될 때 오류로 처리한다.
     * </p>
     *
     * <p>
     * 실무에서는 엑셀 수식을 통한 예기치 않은 값 변조나 보안 이슈를 막기 위해
     * 기본적으로 true를 권장한다.
     * </p>
     */
    private boolean blockFormula;

    /**
     * 오류가 있는 행도 rows에 포함할지 여부.
     *
     * <p>
     * true이면 오류 데이터도 Grid에 표시할 수 있도록 rows에 포함한다.
     * </p>
     */
    private boolean includeErrorRows;

    /**
     * 기본 업로드 옵션을 생성한다.
     *
     * <p>
     * 기본값:
     * 단일 헤더,
     * 첫 번째 행 헤더,
     * 두 번째 행부터 데이터,
     * 빈 행 무시,
     * 수식 차단,
     * 오류 행 포함
     * </p>
     *
     * @return 기본 업로드 옵션
     */
    public static ExcelUploadOption defaultOption() {
        return ExcelUploadOption.builder()
                .useMultiHeader(false)
                .headerStartRowIndex(0)
                .headerEndRowIndex(0)
                .dataStartRowIndex(1)
                .ignoreEmptyRow(true)
                .blockFormula(true)
                .includeErrorRows(true)
                .build();
    }

    /**
     * 다중 헤더용 기본 업로드 옵션을 생성한다.
     *
     * @param headerDepth 헤더 depth
     * @return 다중 헤더 기본 업로드 옵션
     */
    public static ExcelUploadOption defaultMultiHeaderOption(int headerDepth) {

        int resolvedHeaderDepth = Math.max(headerDepth, 1);

        return ExcelUploadOption.builder()
                .useMultiHeader(resolvedHeaderDepth > 1)
                .headerStartRowIndex(0)
                .headerEndRowIndex(resolvedHeaderDepth - 1)
                .dataStartRowIndex(resolvedHeaderDepth)
                .ignoreEmptyRow(true)
                .blockFormula(true)
                .includeErrorRows(true)
                .build();
    }

    /**
     * 3단 헤더용 기본 업로드 옵션을 생성한다.
     *
     * <p>
     * 하위 호환용 메서드다.
     * 동적 헤더 화면에서는 headerDepth를 받는 defaultMultiHeaderOption(int)를 사용하는 것을 권장한다.
     * </p>
     *
     * @return 3단 헤더 기본 업로드 옵션
     */
    public static ExcelUploadOption defaultMultiHeaderOption() {
        return defaultMultiHeaderOption(3);
    }

    public static ExcelUploadOption fromColumns(
            List<ExcelColumnMeta> columns,
            boolean hasExampleRow
    ) {
        int headerDepth = 1;

        if (columns != null && !columns.isEmpty()) {

            for (ExcelColumnMeta column : columns) {

                if (column == null) {
                    continue;
                }

                if (column.getHeaderPath() != null && !column.getHeaderPath().isEmpty()) {

                    int pathDepth = (int) column.getHeaderPath()
                            .stream()
                            .filter(value -> value != null && !value.trim().isEmpty())
                            .count();

                    headerDepth = Math.max(headerDepth, pathDepth);
                    continue;
                }

                if (column.getParentHeader() != null
                        && !column.getParentHeader().trim().isEmpty()) {
                    headerDepth = Math.max(headerDepth, 2);
                }
            }
        }

        return ExcelUploadOption.builder()
                .useMultiHeader(headerDepth > 1)
                .headerStartRowIndex(0)
                .headerEndRowIndex(headerDepth - 1)
                .dataStartRowIndex(hasExampleRow ? headerDepth + 1 : headerDepth)
                .ignoreEmptyRow(true)
                .blockFormula(true)
                .includeErrorRows(true)
                .build();
    }
}

