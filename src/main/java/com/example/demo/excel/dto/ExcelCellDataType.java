package com.example.demo.excel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 엑셀 셀 데이터 타입 enum.
 *
 * <p>
 * 엑셀 업로드 시 셀 값을 어떤 Java 타입으로 변환할지 판단하는 기준이다.
 * </p>
 *
 * <p>
 * 엑셀 다운로드 시에도 값의 타입에 따라 셀 스타일이나 셀 값 세팅 방식을 다르게 적용할 수 있다.
 * </p>
 */
public enum ExcelCellDataType {

    /**
     * 문자열 타입.
     *
     * <p>
     * 고객코드, 고객명, 구분값, 코드값처럼
     * 숫자처럼 보여도 문자로 유지해야 하는 값에 사용한다.
     * </p>
     *
     * <p>
     * 예:
     * C001,
     * 0010,
     * 삼성전자
     * </p>
     */
    STRING(String.class),

    /**
     * Integer 타입.
     *
     * <p>
     * 일반적인 정수 값에 사용한다.
     * </p>
     *
     * <p>
     * 예:
     * 수량,
     * 순번,
     * 개수
     * </p>
     */
    INTEGER(Integer.class),

    /**
     * Long 타입.
     *
     * <p>
     * Integer 범위를 넘을 수 있는 정수 값에 사용한다.
     * </p>
     *
     * <p>
     * 예:
     * 대용량 건수,
     * 긴 숫자 ID
     * </p>
     */
    LONG(Long.class),

    /**
     * BigDecimal 타입.
     *
     * <p>
     * 금액, 비율, 소수점이 필요한 수량 등
     * 정밀 계산이 필요한 숫자 값에 사용한다.
     * </p>
     *
     * <p>
     * Oracle NUMBER(22,10) 같은 컬럼과 매핑할 때 가장 안전하다.
     * </p>
     */
    BIG_DECIMAL(BigDecimal.class),

    /**
     * Boolean 타입.
     *
     * <p>
     * 사용 여부, 체크 여부, Y/N 값을 boolean으로 다루고 싶을 때 사용한다.
     * </p>
     *
     * <p>
     * 실무에서는 Y/N 코드로 쓰는 경우가 많기 때문에
     * 무조건 boolean으로 변환하기보다는 업무 기준에 따라 선택해서 사용한다.
     * </p>
     */
    BOOLEAN(Boolean.class),

    /**
     * LocalDate 타입.
     *
     * <p>
     * 날짜만 필요한 값에 사용한다.
     * </p>
     *
     * <p>
     * 예:
     * 2026-05-29,
     * 20260529
     * </p>
     */
    LOCAL_DATE(LocalDate.class),

    /**
     * LocalDateTime 타입.
     *
     * <p>
     * 날짜와 시간이 모두 필요한 값에 사용한다.
     * </p>
     *
     * <p>
     * 예:
     * 2026-05-29 10:30:00
     * </p>
     */
    LOCAL_DATE_TIME(LocalDateTime.class),

    /**
     * 원본 값 유지 타입.
     *
     * <p>
     * 아직 타입을 확정하기 어렵거나,
     * 엑셀 셀 값을 변환하지 않고 그대로 받고 싶을 때 사용한다.
     * </p>
     */
    OBJECT(Object.class);

    /**
     * 이 enum이 매핑되는 Java 타입.
     */
    private final Class<?> javaType;

    /**
     * ExcelCellDataType 생성자.
     *
     * @param javaType 매핑되는 Java 타입
     */
    ExcelCellDataType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /**
     * Java 타입을 반환한다.
     *
     * @return 매핑 Java 타입
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * 문자열 타입명을 enum으로 안전하게 변환한다.
     *
     * <p>
     * null, 빈 문자열, 잘못된 값이 들어오면 기본값 STRING을 반환한다.
     * 프론트에서 dataType을 누락하거나 오타가 있어도 서버 오류가 나지 않도록 하기 위한 방어 로직이다.
     * </p>
     *
     * @param value 타입 문자열
     * @return ExcelCellDataType
     */
    public static ExcelCellDataType from(String value) {

        // 값이 null이면 기본 타입으로 STRING을 사용한다.
        if (value == null) {
            return STRING;
        }

        // 앞뒤 공백을 제거한다.
        String trimmedValue = value.trim();

        // 빈 문자열이면 기본 타입으로 STRING을 사용한다.
        if (trimmedValue.isEmpty()) {
            return STRING;
        }

        // enum 이름은 대문자 기준이므로 대문자로 변환한다.
        String upperValue = trimmedValue.toUpperCase();

        // 등록된 enum 목록을 순회한다.
        for (ExcelCellDataType type : values()) {

            // enum 이름과 일치하면 해당 타입을 반환한다.
            if (type.name().equals(upperValue)) {
                return type;
            }
        }

        // 일치하는 enum이 없으면 기본 타입으로 STRING을 사용한다.
        return STRING;
    }

    /**
     * 숫자 타입 여부를 반환한다.
     *
     * <p>
     * 업로드 검증에서 숫자 비교,
     * 다운로드에서 숫자 셀 스타일 적용 등에 사용할 수 있다.
     * </p>
     *
     * @return 숫자 타입이면 true
     */
    public boolean isNumberType() {
        return this == INTEGER
                || this == LONG
                || this == BIG_DECIMAL;
    }

    /**
     * 날짜 타입 여부를 반환한다.
     *
     * <p>
     * 업로드 날짜 파싱,
     * 다운로드 날짜 셀 스타일 적용 등에 사용할 수 있다.
     * </p>
     *
     * @return 날짜 타입이면 true
     */
    public boolean isDateType() {
        return this == LOCAL_DATE
                || this == LOCAL_DATE_TIME;
    }

    /**
     * 문자열 타입 여부를 반환한다.
     *
     * @return 문자열 타입이면 true
     */
    public boolean isStringType() {
        return this == STRING;
    }
}