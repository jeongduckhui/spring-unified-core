package com.example.demo.excel.sample;

import com.example.demo.common.response.ApiResult;
import com.example.demo.excel.dto.ExcelUploadRequest;
import com.example.demo.excel.dto.ExcelUploadResult;
import com.example.demo.excel.service.ExcelUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Sales 엑셀 업로드 Controller.
 */
@RestController
@RequiredArgsConstructor
public class SalesExcelController {

    private final ExcelUploadService excelUploadService;

    private final SalesExcelRowValidator salesExcelRowValidator;

    @PostMapping(
            value = "/api/sales/excel/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResult<ExcelUploadResult> uploadSalesExcel(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") ExcelUploadRequest request
    ) {

        ExcelUploadResult result = excelUploadService.upload(
                file,
                request,
                salesExcelRowValidator
        );

        return ApiResult.success(result);
    }
}