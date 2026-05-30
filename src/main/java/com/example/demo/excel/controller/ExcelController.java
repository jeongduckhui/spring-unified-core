package com.example.demo.excel.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.excel.dto.ExcelDownloadRequest;
import com.example.demo.excel.dto.ExcelErrorRowsDownloadRequest;
import com.example.demo.excel.dto.ExcelTemplateDownloadRequest;
import com.example.demo.excel.dto.ExcelUploadRequest;
import com.example.demo.excel.dto.ExcelUploadResult;
import com.example.demo.excel.service.ExcelDownloadService;
import com.example.demo.excel.service.ExcelUploadService;
import com.example.demo.excel.util.ExcelFileNameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 엑셀 공통 Controller.
 *
 * <p>
 * AG Grid 화면에서 사용하는 엑셀 업로드/다운로드 공통 API를 제공한다.
 * </p>
 *
 * <p>
 * 제공 기능:
 * 1. 현재 Grid 데이터 서버 다운로드
 * 2. 템플릿 다운로드
 * 3. 엑셀 업로드
 * 4. 업로드 검증 결과 Grid 표시
 * 5. 오류 행 다운로드
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    /**
     * 엑셀 xlsx Content-Type.
     *
     * <p>
     * Spring의 MediaType에 xlsx 전용 상수가 없기 때문에 문자열로 정의한다.
     * </p>
     */
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * 엑셀 다운로드 서비스.
     */
    private final ExcelDownloadService excelDownloadService;

    /**
     * 엑셀 업로드 서비스.
     */
    private final ExcelUploadService excelUploadService;

    /**
     * 일반 엑셀 다운로드 API.
     *
     * <p>
     * 프론트에서 전달한 columns, rows 기준으로 엑셀 파일을 생성한다.
     * </p>
     *
     * @param request 엑셀 다운로드 요청
     * @return 엑셀 파일 응답
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@RequestBody ExcelDownloadRequest request) {

        // 엑셀 파일 byte 배열을 생성한다.
        byte[] excelBytes = excelDownloadService.createDownloadExcel(request);

        // 다운로드 파일 응답을 생성한다.
        return createExcelFileResponse(request.getFileName(), excelBytes);
    }

    /**
     * 엑셀 템플릿 다운로드 API.
     *
     * <p>
     * 현재 화면의 컬럼 메타 정보를 기준으로 업로드용 템플릿 파일을 생성한다.
     * </p>
     *
     * @param request 엑셀 템플릿 다운로드 요청
     * @return 엑셀 템플릿 파일 응답
     */
    @PostMapping("/template/download")
    public ResponseEntity<byte[]> downloadTemplate(@RequestBody ExcelTemplateDownloadRequest request) {

        // 템플릿 엑셀 파일 byte 배열을 생성한다.
        byte[] excelBytes = excelDownloadService.createTemplateExcel(request);

        // 다운로드 파일 응답을 생성한다.
        return createExcelFileResponse(request.getFileName(), excelBytes);
    }

    /**
     * 엑셀 오류 행 다운로드 API.
     *
     * <p>
     * 업로드 결과 rows 중 _rowStatus = ERROR 인 행만 엑셀 파일로 생성한다.
     * </p>
     *
     * @param request 오류 행 다운로드 요청
     * @return 오류 행 엑셀 파일 응답
     */
    @PostMapping("/error-rows/download")
    public ResponseEntity<byte[]> downloadErrorRows(@RequestBody ExcelErrorRowsDownloadRequest request) {

        // 오류 행 엑셀 파일 byte 배열을 생성한다.
        byte[] excelBytes = excelDownloadService.createErrorRowsExcel(request);

        // 다운로드 파일 응답을 생성한다.
        return createExcelFileResponse(request.getFileName(), excelBytes);
    }

    /**
     * 엑셀 업로드 API.
     *
     * <p>
     * 업로드된 엑셀 파일을 즉시 DB에 저장하지 않고,
     * 서버에서 검증한 뒤 Grid에 표시할 수 있는 결과를 반환한다.
     * </p>
     *
     * <p>
     * 처리 흐름:
     * 업로드
     * → 헤더 검증
     * → 필수값 검증
     * → 타입 변환
     * → 오류 목록 생성
     * → Grid 표시
     * → 사용자 확인 후 별도 저장 API 호출
     * </p>
     *
     * @param file 업로드 엑셀 파일
     * @param request 업로드 처리 기준 요청
     * @return 업로드 검증 결과
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResult<ExcelUploadResult> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") ExcelUploadRequest request
    ) {

        // 엑셀 업로드 파일을 읽고 검증 결과를 생성한다.
        ExcelUploadResult result = excelUploadService.upload(file, request);

        log.info("option={}", request.getOption());

        // 공통 응답 형태로 반환한다.
        return ApiResult.success(result);
    }

    /**
     * 엑셀 파일 다운로드 응답을 생성한다.
     *
     * @param fileName 파일명
     * @param excelBytes 엑셀 byte 배열
     * @return ResponseEntity
     */
    private ResponseEntity<byte[]> createExcelFileResponse(
            String fileName,
            byte[] excelBytes
    ) {

        // Content-Disposition 헤더 값을 생성한다.
        String contentDisposition = ExcelFileNameUtils.buildContentDisposition(fileName);

        // 파일 다운로드 응답을 반환한다.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .body(excelBytes);
    }
}