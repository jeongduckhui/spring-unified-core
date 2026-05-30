package com.example.demo.excel.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 엑셀 파일명 처리 유틸 클래스.
 *
 * <p>
 * 엑셀 다운로드 응답에서 사용할 파일명 생성,
 * 확장자 보정,
 * Content-Disposition 헤더용 인코딩을 담당한다.
 * </p>
 */
public final class ExcelFileNameUtils {

    /**
     * 엑셀 확장자.
     */
    private static final String XLSX_EXTENSION = ".xlsx";

    /**
     * 기본 파일명.
     */
    private static final String DEFAULT_FILE_NAME = "excel-download";

    /**
     * 날짜/시간 suffix 포맷.
     */
    private static final DateTimeFormatter FILE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 유틸 클래스이므로 외부에서 생성하지 못하게 막는다.
     */
    private ExcelFileNameUtils() {
    }

    /**
     * 요청 파일명이 비어 있으면 기본 파일명을 반환하고,
     * .xlsx 확장자가 없으면 자동으로 붙여준다.
     *
     * @param fileName 요청 파일명
     * @return 보정된 파일명
     */
    public static String normalizeXlsxFileName(String fileName) {

        // null 또는 빈 값이면 기본 파일명을 사용한다.
        if (fileName == null || fileName.trim().isEmpty()) {
            return DEFAULT_FILE_NAME + "_" + nowSuffix() + XLSX_EXTENSION;
        }

        // 앞뒤 공백을 제거한다.
        String normalizedFileName = fileName.trim();

        // 이미 .xlsx 확장자로 끝나면 그대로 반환한다.
        if (normalizedFileName.toLowerCase().endsWith(XLSX_EXTENSION)) {
            return normalizedFileName;
        }

        // 확장자가 없으면 .xlsx를 붙여 반환한다.
        return normalizedFileName + XLSX_EXTENSION;
    }

    /**
     * Content-Disposition 헤더에 사용할 파일명을 인코딩한다.
     *
     * <p>
     * 한글 파일명 다운로드 깨짐을 방지하기 위해 UTF-8로 인코딩한다.
     * 공백은 브라우저 호환성을 위해 + 대신 %20으로 변환한다.
     * </p>
     *
     * @param fileName 파일명
     * @return 인코딩된 파일명
     */
    public static String encodeFileName(String fileName) {

        // UTF-8로 URL 인코딩한다.
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    /**
     * HTTP 다운로드 응답에 사용할 Content-Disposition 값을 생성한다.
     *
     * <p>
     * RFC 5987 방식의 filename*=UTF-8'' 형식을 사용한다.
     * </p>
     *
     * @param fileName 파일명
     * @return Content-Disposition 헤더 값
     */
    public static String buildContentDisposition(String fileName) {

        // 파일명을 xlsx 기준으로 보정한다.
        String normalizedFileName = normalizeXlsxFileName(fileName);

        // 한글 파일명을 UTF-8로 인코딩한다.
        String encodedFileName = encodeFileName(normalizedFileName);

        // attachment 다운로드 헤더 값을 반환한다.
        return "attachment; filename*=UTF-8''" + encodedFileName;
    }

    /**
     * 현재 날짜/시간 suffix를 반환한다.
     *
     * @return yyyyMMdd_HHmmss 형식 문자열
     */
    private static String nowSuffix() {
        return LocalDateTime.now().format(FILE_TIME_FORMATTER);
    }
}