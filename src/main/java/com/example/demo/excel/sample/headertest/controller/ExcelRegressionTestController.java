package com.example.demo.excel.sample.headertest.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelUploadRequest;
import com.example.demo.excel.dto.ExcelUploadResult;
import com.example.demo.excel.sample.headertest.dto.HeaderTestRequest;
import com.example.demo.excel.sample.headertest.service.ExcelRegressionTestService;
import com.example.demo.excel.service.ExcelUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excel/regression")
public class ExcelRegressionTestController {

    private final ExcelRegressionTestService service;
    private final ExcelUploadService excelUploadService;

    @GetMapping("/columns")
    public ApiResult<List<ExcelColumnMeta>> getColumns(
            @RequestParam int level
    ) {

        return ApiResult.success(
                service.getColumns(level)
        );
    }

    @GetMapping("/rows")
    public ApiResult<List<Map<String,Object>>> getRows(
            @RequestParam int level
    ) {

        return ApiResult.success(
                service.getRows(level)
        );
    }

    @PostMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestBody HeaderTestRequest request
    ) {

        log.info(
                "headerLevel={}",
                request.getHeaderLevel()
        );

        byte[] excelBytes =
                service.downloadTemplate(
                        request.getHeaderLevel()
                );

        String fileName =
                "header-level-" +
                        request.getHeaderLevel() +
                        "-template.xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                fileName +
                                "\""
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .contentLength(excelBytes.length)
                .body(excelBytes);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResult<ExcelUploadResult> upload(
            @RequestPart("file")
            MultipartFile file,

            @RequestPart("request")
            ExcelUploadRequest request
    ) {

        ExcelUploadResult result =
                excelUploadService.upload(
                        file,
                        request
                );

        return ApiResult.success(result);
    }
}