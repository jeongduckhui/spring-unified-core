package com.example.demo.excel.util;

import com.example.demo.excel.dto.ExcelCellDataType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 엑셀 셀 읽기 유틸 클래스.
 *
 * <p>
 * 업로드된 엑셀 파일에서 셀 값을 읽고,
 * ExcelCellDataType 기준으로 Java 타입으로 변환한다.
 * </p>
 *
 * <p>
 * 이 클래스는 수식 셀 차단, 빈 셀 판단, 타입 자동 변환을 담당한다.
 * </p>
 */
public final class ExcelCellReadUtils {

    /**
     * 유틸 클래스이므로 외부에서 생성하지 못하게 막는다.
     */
    private ExcelCellReadUtils() {
    }

    /**
     * 셀이 수식 셀인지 확인한다.
     *
     * @param cell 엑셀 셀
     * @return 수식 셀이면 true
     */
    public static boolean isFormulaCell(Cell cell) {

        // 셀이 null이면 수식 셀이 아니다.
        if (cell == null) {
            return false;
        }

        // 셀 타입이 FORMULA인지 확인한다.
        return cell.getCellType() == CellType.FORMULA;
    }

    /**
     * 셀이 비어 있는지 확인한다.
     *
     * @param cell 엑셀 셀
     * @return 비어 있으면 true
     */
    public static boolean isBlankCell(Cell cell) {

        // 셀이 null이면 빈 셀이다.
        if (cell == null) {
            return true;
        }

        // 셀 타입이 BLANK이면 빈 셀이다.
        if (cell.getCellType() == CellType.BLANK) {
            return true;
        }

        // 문자열 셀인데 trim 결과가 비어 있으면 빈 셀이다.
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue() == null
                    || cell.getStringCellValue().trim().isEmpty();
        }

        // 그 외 타입은 빈 셀이 아니다.
        return false;
    }

    /**
     * 셀 값을 문자열로 읽는다.
     *
     * <p>
     * 헤더 검증에서는 타입 변환보다 원본 표시 문자열이 중요하므로 이 메서드를 사용한다.
     * </p>
     *
     * @param cell 엑셀 셀
     * @return 문자열 값
     */
    public static String readAsString(Cell cell) {

        // 셀이 null이면 빈 문자열을 반환한다.
        if (cell == null) {
            return "";
        }

        // 셀 타입에 따라 문자열로 변환한다.
        return switch (cell.getCellType()) {

            // 문자열 셀은 trim 후 반환한다.
            case STRING -> cell.getStringCellValue() == null
                    ? ""
                    : cell.getStringCellValue().trim();

            // 숫자 셀은 소수점 불필요 여부를 판단해 문자열로 변환한다.
            case NUMERIC -> {
                double numberValue = cell.getNumericCellValue();

                // 정수처럼 보이는 숫자는 long 문자열로 반환한다.
                if (numberValue == Math.floor(numberValue)) {
                    yield String.valueOf((long) numberValue);
                }

                // 소수점이 있는 숫자는 BigDecimal 문자열로 반환한다.
                yield BigDecimal.valueOf(numberValue).stripTrailingZeros().toPlainString();
            }

            // Boolean 셀은 문자열로 반환한다.
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

            // 수식 셀은 수식 문자열을 반환한다.
            case FORMULA -> cell.getCellFormula();

            // 빈 셀 또는 기타 타입은 빈 문자열을 반환한다.
            default -> "";
        };
    }

    /**
     * 셀 값을 지정한 타입으로 변환한다.
     *
     * @param cell 엑셀 셀
     * @param dataType 변환 대상 타입
     * @return 변환된 값
     */
    public static Object readValue(Cell cell, ExcelCellDataType dataType) {

        // 셀이 비어 있으면 null을 반환한다.
        if (isBlankCell(cell)) {
            return null;
        }

        // 타입이 null이면 STRING으로 처리한다.
        ExcelCellDataType resolvedType = dataType == null
                ? ExcelCellDataType.STRING
                : dataType;

        // 타입별 변환을 수행한다.
        return switch (resolvedType) {
            case STRING -> readAsString(cell);
            case INTEGER -> readInteger(cell);
            case LONG -> readLong(cell);
            case BIG_DECIMAL -> readBigDecimal(cell);
            case BOOLEAN -> readBoolean(cell);
            case LOCAL_DATE -> readLocalDate(cell);
            case LOCAL_DATE_TIME -> readLocalDateTime(cell);
            case OBJECT -> readRawValue(cell);
        };
    }

    /**
     * 셀 값을 Integer로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return Integer 값
     */
    private static Integer readInteger(Cell cell) {

        // BigDecimal로 먼저 변환한다.
        BigDecimal value = readBigDecimal(cell);

        // 값이 null이면 null 반환.
        if (value == null) {
            return null;
        }

        // Integer로 변환한다.
        return value.intValue();
    }

    /**
     * 셀 값을 Long으로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return Long 값
     */
    private static Long readLong(Cell cell) {

        // BigDecimal로 먼저 변환한다.
        BigDecimal value = readBigDecimal(cell);

        // 값이 null이면 null 반환.
        if (value == null) {
            return null;
        }

        // Long으로 변환한다.
        return value.longValue();
    }

    /**
     * 셀 값을 BigDecimal로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return BigDecimal 값
     */
    private static BigDecimal readBigDecimal(Cell cell) {

        // 셀이 null이면 null 반환.
        if (cell == null) {
            return null;
        }

        // 셀 타입별로 숫자 변환을 수행한다.
        return switch (cell.getCellType()) {

            // 숫자 셀은 BigDecimal로 변환한다.
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());

            // 문자열 셀은 문자열을 정리한 뒤 BigDecimal로 변환한다.
            case STRING -> {
                String value = cell.getStringCellValue();

                if (value == null || value.trim().isEmpty()) {
                    yield null;
                }

                yield new BigDecimal(value.trim().replace(",", ""));
            }

            // 그 외 타입은 문자열 변환 후 BigDecimal로 시도한다.
            default -> {
                String value = readAsString(cell);

                if (value == null || value.trim().isEmpty()) {
                    yield null;
                }

                yield new BigDecimal(value.trim().replace(",", ""));
            }
        };
    }

    /**
     * 셀 값을 Boolean으로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return Boolean 값
     */
    private static Boolean readBoolean(Cell cell) {

        // 셀이 null이면 null 반환.
        if (cell == null) {
            return null;
        }

        // 셀 타입별 Boolean 변환.
        return switch (cell.getCellType()) {

            // Boolean 셀은 그대로 반환.
            case BOOLEAN -> cell.getBooleanCellValue();

            // 문자열 셀은 true/false, Y/N, 1/0 정도를 지원한다.
            case STRING -> {
                String value = cell.getStringCellValue();

                if (value == null || value.trim().isEmpty()) {
                    yield null;
                }

                String normalized = value.trim().toUpperCase();

                if ("Y".equals(normalized) || "TRUE".equals(normalized) || "1".equals(normalized)) {
                    yield true;
                }

                if ("N".equals(normalized) || "FALSE".equals(normalized) || "0".equals(normalized)) {
                    yield false;
                }

                yield Boolean.valueOf(normalized);
            }

            // 숫자 셀은 0이면 false, 그 외 true.
            case NUMERIC -> cell.getNumericCellValue() != 0;

            // 그 외 타입은 null.
            default -> null;
        };
    }

    /**
     * 셀 값을 LocalDate로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return LocalDate 값
     */
    private static LocalDate readLocalDate(Cell cell) {

        // 셀이 null이면 null 반환.
        if (cell == null) {
            return null;
        }

        // 엑셀 날짜 숫자 셀인 경우.
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // 문자열 날짜를 파싱한다.
        String value = readAsString(cell);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 기본은 ISO 형식 yyyy-MM-dd 기준.
        return LocalDate.parse(value.trim());
    }

    /**
     * 셀 값을 LocalDateTime으로 변환한다.
     *
     * @param cell 엑셀 셀
     * @return LocalDateTime 값
     */
    private static LocalDateTime readLocalDateTime(Cell cell) {

        // 셀이 null이면 null 반환.
        if (cell == null) {
            return null;
        }

        // 엑셀 날짜 숫자 셀인 경우.
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        // 문자열 일시를 파싱한다.
        String value = readAsString(cell);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 기본은 ISO 형식 yyyy-MM-ddTHH:mm:ss 기준.
        return LocalDateTime.parse(value.trim());
    }

    /**
     * 셀 원본 값을 최대한 보존해서 읽는다.
     *
     * @param cell 엑셀 셀
     * @return 원본에 가까운 값
     */
    private static Object readRawValue(Cell cell) {

        // 셀이 null이면 null 반환.
        if (cell == null) {
            return null;
        }

        // 셀 타입별 원본 값 반환.
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}