package com.example.demo.excel.sample.headertest.service;

import com.example.demo.excel.dto.ExcelCellDataType;
import com.example.demo.excel.dto.ExcelColumnMeta;
import com.example.demo.excel.dto.ExcelTemplateDownloadRequest;
import com.example.demo.excel.service.ExcelDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelRegressionTestService {

    private final ExcelDownloadService excelDownloadService;

    public List<ExcelColumnMeta> getColumns(
            int level
    ) {

        return switch (level) {

            case 1 -> buildLevel1Columns();

            case 2 -> buildLevel2Columns();

            case 3 -> buildLevel3Columns();

            default -> throw new IllegalArgumentException(
                    "지원하지 않는 헤더 레벨 : " + level
            );
        };
    }

    public List<Map<String,Object>> getRows(
            int level
    ) {

        Map<String,Object> row = new LinkedHashMap<>();

        row.put("companyCode", "1000");
        row.put("companyName", "교보문고");

        row.put("sales2026", 100);
        row.put("sales2027", 200);

        row.put("qty2026_1Q", 10);
        row.put("qty2026_2Q", 20);
        row.put("qty2026_3Q", 30);
        row.put("qty2026_4Q", 40);

        row.put("amt2026_1Q", 1000);
        row.put("amt2026_2Q", 2000);
        row.put("amt2026_3Q", 3000);
        row.put("amt2026_4Q", 4000);

        return List.of(row);
    }

    private List<ExcelColumnMeta> buildLevel1Columns() {

        return List.of(

                ExcelColumnMeta.builder()
                        .field("companyCode")
                        .headerName("회사코드")
                        .required(true)
                        .exampleValue("1000")
                        .build(),

                ExcelColumnMeta.builder()
                        .field("companyName")
                        .headerName("회사명")
                        .required(true)
                        .exampleValue("교보문고")
                        .build(),

                ExcelColumnMeta.builder()
                        .field("sales2026")
                        .headerName("2026")
                        .exampleValue("100")
                        .dataType(ExcelCellDataType.INTEGER)
                        .build(),

                ExcelColumnMeta.builder()
                        .field("sales2027")
                        .headerName("2027")
                        .exampleValue("200")
                        .dataType(ExcelCellDataType.INTEGER)
                        .build()
        );
    }

    private List<ExcelColumnMeta> buildLevel2Columns() {

        return List.of(

                ExcelColumnMeta.builder()
                        .field("companyCode")
                        .headerPath(List.of(
                                "회사정보",
                                "회사코드"
                        ))
                        .required(true)
                        .exampleValue("1000")
                        .build(),

                ExcelColumnMeta.builder()
                        .field("companyName")
                        .headerPath(List.of(
                                "회사정보",
                                "회사명"
                        ))
                        .required(true)
                        .exampleValue("교보문고")
                        .build(),

                ExcelColumnMeta.builder()
                        .field("sales2026")
                        .headerPath(List.of(
                                "매출",
                                "2026"
                        ))
                        .exampleValue("100")
                        .dataType(ExcelCellDataType.INTEGER)
                        .build(),

                ExcelColumnMeta.builder()
                        .field("sales2027")
                        .headerPath(List.of(
                                "매출",
                                "2027"
                        ))
                        .exampleValue("200")
                        .dataType(ExcelCellDataType.INTEGER)
                        .build()
        );
    }

    private List<ExcelColumnMeta> buildLevel3Columns() {

        List<ExcelColumnMeta> columns = new ArrayList<>();

        for (String quarter : List.of(
                "1Q",
                "2Q",
                "3Q",
                "4Q"
        )) {

            columns.add(
                    ExcelColumnMeta.builder()
                            .field(
                                    "qty2026_" + quarter
                            )
                            .headerPath(List.of(
                                    "QTY",
                                    "2026",
                                    quarter
                            ))
                            .dataType(
                                    ExcelCellDataType.INTEGER
                            )
                            .exampleValue("100")
                            .build()
            );
        }

        for (String quarter : List.of(
                "1Q",
                "2Q",
                "3Q",
                "4Q"
        )) {

            columns.add(
                    ExcelColumnMeta.builder()
                            .field(
                                    "amt2026_" + quarter
                            )
                            .headerPath(List.of(
                                    "AMT",
                                    "2026",
                                    quarter
                            ))
                            .dataType(
                                    ExcelCellDataType.INTEGER
                            )
                            .exampleValue("1000")
                            .build()
            );
        }

        return columns;
    }

    public byte[] downloadTemplate(
            int level
    ) {


        log.info(
                "level={}, columns={}",
                level,
                getColumns(level).size()
        );

        List<ExcelColumnMeta> columns =
                getColumns(level);

        ExcelTemplateDownloadRequest request =
                ExcelTemplateDownloadRequest.builder()
                        .fileName(
                                "header-level-" +
                                        level +
                                        "-template.xlsx"
                        )
                        .sheetName("HeaderTest")
                        .columns(columns)

                        // 예제 데이터 생성
                        .includeExampleRow(true)

                        // hidden 컬럼 제거
                        .excludeHiddenColumns(true)

                        // 2단, 3단 헤더 생성
                        .useMultiHeader(level > 1)

                        .build();

        return excelDownloadService
                .createTemplateExcel(request);
    }
}