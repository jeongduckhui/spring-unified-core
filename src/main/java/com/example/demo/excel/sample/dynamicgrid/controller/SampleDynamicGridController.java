package com.example.demo.excel.sample.dynamicgrid.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.excel.dto.ExcelUploadRequest;
import com.example.demo.excel.dto.ExcelUploadResult;
import com.example.demo.excel.sample.dynamicgrid.excel.SampleDynamicGridExcelValidator;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSaveRequest;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSearchRequest;
import com.example.demo.excel.sample.dynamicgrid.service.SampleDynamicGridJpaSaveService;
import com.example.demo.excel.sample.dynamicgrid.service.SampleDynamicGridMyBatisSaveService;
import com.example.demo.excel.sample.dynamicgrid.service.SampleDynamicGridService;
import com.example.demo.excel.service.ExcelUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 엑셀 샘플 다이나믹 그리드 Controller.
 *
 * <p>
 * 이 Controller는 엑셀 공통 모듈을 실제 실무형 AG Grid 화면에 붙이는 샘플 API를 제공한다.
 * </p>
 *
 * <p>
 * 주요 기능:
 * 1. MyBatis 조회
 * 2. MyBatis 저장
 * 3. JPA 저장
 * 4. 조회조건 기준 전체 엑셀 다운로드
 * 5. 엑셀 업로드 후 Grid 표시용 검증 결과 반환
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/excel/sample/dynamic-grid")
//@RequestMapping("/test/api/excel/sample/dynamic-grid")
@RequiredArgsConstructor
public class SampleDynamicGridController {

    /**
     * 다이나믹 그리드 조회/공통 변환 서비스.
     */
    private final SampleDynamicGridService sampleDynamicGridService;

    /**
     * MyBatis 저장 서비스.
     */
    private final SampleDynamicGridMyBatisSaveService sampleDynamicGridMyBatisSaveService;

    /**
     * JPA 저장 서비스.
     */
    private final SampleDynamicGridJpaSaveService sampleDynamicGridJpaSaveService;

    /**
     * 공통 엑셀 업로드 서비스.
     */
    private final ExcelUploadService excelUploadService;

    /**
     * 다이나믹 그리드 엑셀 업무 검증 Validator.
     */
    private final SampleDynamicGridExcelValidator sampleDynamicGridExcelValidator;

    /**
     * 다이나믹 그리드 조회 API.
     *
     * <p>
     * 조회는 MyBatis를 사용한다.
     * DB에는 세로형으로 저장된 데이터를 조회하고,
     * Service에서 AG Grid 표시용 가로형 rowData로 변환해서 반환한다.
     * </p>
     *
     * @param request 조회조건
     * @return AG Grid rowData
     */
    @PostMapping("/search")
    public ApiResult<List<Map<String, Object>>> search(
            @RequestBody SampleDynamicGridSearchRequest request
    ) {

        // 조회조건 기준으로 Grid rowData를 조회한다.
        List<Map<String, Object>> rows = sampleDynamicGridService.search(request);

        // 공통 응답으로 반환한다.
        return ApiResult.success(rows);
    }

    /**
     * 다이나믹 그리드 MyBatis 저장 API.
     *
     * <p>
     * 프론트 Grid의 가로형 rowData를 서버에서 세로형 cell 데이터로 변환한 뒤,
     * MyBatis Mapper를 이용해 delete 후 insert 방식으로 저장한다.
     * </p>
     *
     * @param request 저장 요청
     * @return 성공 응답
     */
    @PostMapping("/save/mybatis")
    public ApiResult<Void> saveByMyBatis(
            @RequestBody SampleDynamicGridSaveRequest request
    ) {

        // MyBatis 방식으로 저장한다.
        sampleDynamicGridMyBatisSaveService.save(request);

        // 성공 응답을 반환한다.
        return ApiResult.success("엑셀 저장");
    }

    /**
     * 다이나믹 그리드 JPA 저장 API.
     *
     * <p>
     * 프론트 Grid의 가로형 rowData를 서버에서 세로형 cell 데이터로 변환한 뒤,
     * JPA Repository를 이용해 delete 후 saveAll 방식으로 저장한다.
     * </p>
     *
     * @param request 저장 요청
     * @return 성공 응답
     */
    @PostMapping("/save/jpa")
    public ApiResult<Void> saveByJpa(
            @RequestBody SampleDynamicGridSaveRequest request
    ) {

        // JPA 방식으로 저장한다.
        sampleDynamicGridJpaSaveService.save(request);

        // 성공 응답을 반환한다.
        return ApiResult.success("엑셀 저장");
    }

    /**
     * 조회조건 기준 전체 엑셀 다운로드 API.
     *
     * <p>
     * 현재 Grid에 표시된 rowData를 다운로드하는 것이 아니라,
     * 조회조건 기준으로 서버에서 전체 데이터를 다시 조회한 뒤 엑셀로 생성한다.
     * </p>
     *
     * <p>
     * 예:
     * 화면에는 페이징으로 10건만 표시되어 있어도,
     * 다운로드는 조회조건 기준 전체 데이터를 대상으로 생성할 수 있다.
     * </p>
     *
     * @param request 조회조건 + 엑셀 컬럼 메타
     * @return 엑셀 파일 응답
     */
    @PostMapping("/excel/download-all")
    public ResponseEntity<byte[]> downloadAll(
            @RequestBody SampleDynamicGridSearchRequest request
    ) {

        // 조회조건 기준 전체 데이터를 엑셀로 생성한다.
        return sampleDynamicGridService.downloadAll(request);
    }

    /**
     * 다이나믹 그리드 엑셀 업로드 API.
     *
     * <p>
     * 공통 ExcelUploadService를 사용하되,
     * SampleDynamicGridExcelValidator를 추가로 적용해서 업무 검증까지 수행한다.
     * </p>
     *
     * <p>
     * 업로드 후 즉시 저장하지 않는다.
     * 서버 검증 결과를 Grid에 표시하고,
     * 사용자가 확인한 뒤 save/mybatis 또는 save/jpa API를 호출해서 저장한다.
     * </p>
     *
     * @param file 업로드 엑셀 파일
     * @param request 엑셀 업로드 요청 메타
     * @return 업로드 검증 결과
     */
    @PostMapping(
            value = "/excel/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResult<ExcelUploadResult> uploadExcel(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") ExcelUploadRequest request
    ) {

        log.info("option={}", request.getOption());

        // 공통 업로드 서비스 + 업무 Validator로 업로드 검증을 수행한다.
        /*
        ExcelUploadResult result = excelUploadService.upload(
                file,
                request,
                sampleDynamicGridExcelValidator
        );
        */

        ExcelUploadResult result = excelUploadService.uploadWithDrmDecrypt(
                file,
                request,
                sampleDynamicGridExcelValidator);

        // 공통 응답으로 반환한다.
        return ApiResult.success(result);
    }
}